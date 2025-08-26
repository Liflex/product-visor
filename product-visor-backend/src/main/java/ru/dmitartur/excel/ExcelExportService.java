package ru.dmitartur.excel;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.dmitartur.entity.Product;
import ru.dmitartur.entity.ProductAttributeValue;
import ru.dmitartur.repository.ProductRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final ProductRepository productRepository;

    public byte[] exportLensExcel(List<Long> productIds) {
        try {
            if (productIds == null || productIds.isEmpty()) {
                throw new IllegalArgumentException("productIds is empty");
            }
            // Делегируем специализированному заполнятору формы линз
            OzonLensesFormFiller filler = new OzonLensesFormFiller();
            List<LensExcelDto> dtos = productIds.stream()
                    .map(id -> productRepository.findById(id).orElseThrow())
                    .map(this::toLensDto)
                    .collect(java.util.stream.Collectors.toList());
            return filler.fillFromTemplate(dtos);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Excel export failed", e);
        }
    }

    private byte[] multiSheet(List<Long> productIds) throws IOException {
        ClassPathResource res = new ClassPathResource("form.xlsx");
        try (InputStream in = res.getInputStream(); Workbook wb = new XSSFWorkbook(in)) {
            // Первый лист используем как шаблон, копируем для каждого товара
            Sheet template = wb.getSheetAt(0);
            for (int i = 0; i < productIds.size(); i++) {
                Product p = productRepository.findById(productIds.get(i)).orElseThrow();
                LensExcelDto dto = toLensDto(p);
                Sheet sheet = (i == 0) ? template : wb.cloneSheet(0);
                if (i > 0) {
                    wb.setSheetName(wb.getSheetIndex(sheet), "Товар " + (i + 1));
                }
                replacePlaceholders(sheet, dto);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    private byte[] fillTemplate(LensExcelDto dto) throws IOException {
        ClassPathResource res = new ClassPathResource("form.xlsx");
        try (InputStream in = res.getInputStream(); Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            replacePlaceholders(sheet, dto);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    private void replacePlaceholders(Sheet sheet, LensExcelDto dto) {
        Map<String, String> map = new HashMap<>();
        map.put("${NAME}", n(dto.getProductName()));
        map.put("${ARTICLE}", n(dto.getArticle()));
        map.put("${BARCODE}", n(dto.getBarcode()));
        map.put("${PRICE}", n(dto.getPrice()));
        map.put("${CATEGORY}", n(dto.getCategoryName()));
        map.put("${QUANTITY}", n(dto.getQuantity()));
        map.put("${PKG_WIDTH}", n(dto.getPackageWidth()));
        map.put("${PKG_HEIGHT}", n(dto.getPackageHeight()));
        map.put("${PKG_LENGTH}", n(dto.getPackageLength()));
        map.put("${PKG_WEIGHT}", n(dto.getPackageWeight()));
        map.put("${PKG_QTY}", n(dto.getPackageQuantityInPack()));
        map.put("${COLOR}", n(dto.getAttrColor()));
        map.put("${DIOPTRIES}", n(dto.getAttrDioptries()));
        map.put("${DATE_EXPIRED}", n(dto.getAttrDateExpired()));
        map.put("${DIAMETER}", n(dto.getAttrDiameter()));
        map.put("${CURVATURE}", n(dto.getAttrCurvature()));

        for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            for (int c = 0; c < row.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                if (cell == null) continue;
                if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                    String text = cell.getStringCellValue();
                    if (text != null && text.contains("${")) {
                        String replaced = text;
                        for (Map.Entry<String, String> e : map.entrySet()) {
                            if (replaced.contains(e.getKey())) {
                                replaced = replaced.replace(e.getKey(), e.getValue());
                            }
                        }
                        cell.setCellValue(replaced);
                    }
                }
            }
        }
    }

    private String n(String s) { return s == null ? "" : s; }

    private LensExcelDto toLensDto(Product p) {
        LensExcelDto dto = new LensExcelDto();
        dto.setProductName(p.getName());
        dto.setArticle(p.getArticle());
        dto.setBarcode(p.getBarcode());
        dto.setPrice(p.getPrice() == null ? "" : String.valueOf(p.getPrice()));
        dto.setCategoryName(p.getCategory() == null ? "" : p.getCategory().getName());
        //TODO нужно будет поправить чтобы quantity доставалось из выбранного productStock типа.
//        dto.setQuantity(p.getQuantity() == null ? "" : String.valueOf(p.getQuantity()));
        if (p.getPackageInfo() != null) {
            var pkg = p.getPackageInfo();
            dto.setPackageWidth(s(pkg.getWidth()));
            dto.setPackageHeight(s(pkg.getHeight()));
            dto.setPackageLength(s(pkg.getLength()));
            dto.setPackageWeight(s(pkg.getWeight()));
            dto.setPackageQuantityInPack(pkg.getQuantityInPackage() == null ? "" : String.valueOf(pkg.getQuantityInPackage()));
        }
        // атрибуты
        if (p.getProductAttributeValues() != null) {
            Map<String, List<ProductAttributeValue>> byName = p.getProductAttributeValues().stream()
                    .filter(av -> av.getAttribute() != null && av.getAttribute().getName() != null)
                    .collect(Collectors.groupingBy(av -> av.getAttribute().getName()));
            dto.setAttrColor(joinVals(byName.get("color")));
            dto.setAttrDioptries(joinVals(byName.get("dioptries")));
            dto.setAttrDateExpired(joinVals(byName.get("date_expired")));
            dto.setAttrDiameter(joinVals(byName.get("diameter")));
            dto.setAttrCurvature(joinVals(byName.get("curvature")));
        }
        return dto;
    }

    private String s(Double d) { return d == null ? "" : String.valueOf(d); }

    private String joinVals(List<ProductAttributeValue> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream()
                .map(ProductAttributeValue::getValue)
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining("; "));
    }

    // ===================== Row-based filling =====================

    private static class ConfigIndices {
        int headerRowIndex = 1;      // по умолчанию строка заголовков = 1 (0-based)
        int firstDataRowIndex = 4;   // по умолчанию первая строка данных = 4 (0-based)
    }

    private ConfigIndices readConfigIndices(Workbook wb) {
        ConfigIndices cfg = new ConfigIndices();
        Sheet cfgSheet = wb.getSheet("configs");
        if (cfgSheet == null) return cfg;
        for (int r = cfgSheet.getFirstRowNum(); r <= cfgSheet.getLastRowNum(); r++) {
            Row row = cfgSheet.getRow(r);
            if (row == null) continue;
            Cell keyCell = row.getCell(0);
            Cell valCell = row.getCell(1);
            if (keyCell == null || valCell == null || keyCell.getCellType() != org.apache.poi.ss.usermodel.CellType.STRING) continue;
            String key = keyCell.getStringCellValue();
            String val = valCell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                    ? String.valueOf((int) valCell.getNumericCellValue())
                    : valCell.getStringCellValue();
            if (key == null || val == null) continue;
            key = key.trim();
            val = val.trim();
            try {
                if ("PRODUCTS_TITLE_ROW_INDEX".equalsIgnoreCase(key)) {
                    // значение в файле 1-based, переводим в 0-based
                    cfg.headerRowIndex = Integer.parseInt(val) - 1;
                } else if ("PRODUCTS_FIRST_DATA_ROW_INDEX".equalsIgnoreCase(key)) {
                    cfg.firstDataRowIndex = Integer.parseInt(val) - 1;
                }
            } catch (NumberFormatException ignored) { }
        }
        return cfg;
    }

    private static class ColumnMap {
        int colArticle = -1;
        int colName = -1;
        int colPrice = -1;
        int colBarcode = -1;
        int colPkgWeightG = -1;
        int colPkgWidthMm = -1;
        int colPkgHeightMm = -1;
        int colPkgLengthMm = -1;
        int colQtyInPack = -1;
        int colColor = -1;
        int colDioptries = -1;
        int colCurvature = -1;
        int colDiameterMm = -1;
        int colSizeCm = -1; // Размер упаковки (Длина х Ширина х Высота), см
    }

    private ColumnMap buildColumnMap(Sheet sheet, int headerRowIdx) {
        ColumnMap map = new ColumnMap();
        Row header = sheet.getRow(headerRowIdx);
        if (header == null) return map;
        for (int c = 0; c < header.getLastCellNum(); c++) {
            Cell cell = header.getCell(c);
            if (cell == null || cell.getCellType() != org.apache.poi.ss.usermodel.CellType.STRING) continue;
            String h = cell.getStringCellValue();
            if (h == null) continue;
            h = h.trim();
            switch (h) {
                case "Артикул*" -> map.colArticle = c;
                case "Название товара" -> map.colName = c;
                case "Цена, руб.*" -> map.colPrice = c;
                case "Штрихкод (Серийный номер / EAN)" -> map.colBarcode = c;
                case "Вес в упаковке, г*" -> map.colPkgWeightG = c;
                case "Ширина упаковки, мм*" -> map.colPkgWidthMm = c;
                case "Высота упаковки, мм*" -> map.colPkgHeightMm = c;
                case "Длина упаковки, мм*" -> map.colPkgLengthMm = c;
                case "Количество в упаковке, шт" -> map.colQtyInPack = c;
                case "Цвет товара" -> map.colColor = c;
                case "Оптическая сила" -> map.colDioptries = c;
                case "Радиус кривизны" -> map.colCurvature = c;
                case "Диаметр, мм" -> map.colDiameterMm = c;
                case "Размер упаковки (Длина х Ширина х Высота), см" -> map.colSizeCm = c;
                default -> {
                }
            }
        }
        return map;
    }

    private void writeRow(Sheet sheet, int rowIdx, ColumnMap cols, LensExcelDto dto) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) row = sheet.createRow(rowIdx);
        // Простые поля
        setStringCell(row, cols.colArticle, dto.getArticle());
        setStringCell(row, cols.colName, dto.getProductName());
        setStringCell(row, cols.colPrice, dto.getPrice());
        setStringCell(row, cols.colBarcode, dto.getBarcode());
        setStringCell(row, cols.colQtyInPack, dto.getPackageQuantityInPack());
        setStringCell(row, cols.colColor, dto.getAttrColor());
        setStringCell(row, cols.colDioptries, dto.getAttrDioptries());
        setStringCell(row, cols.colCurvature, dto.getAttrCurvature());
        setStringCell(row, cols.colDiameterMm, dto.getAttrDiameter());

        // Конвертации единиц: cm->mm, kg->g
        setStringCell(row, cols.colPkgWidthMm, cmToMm(dto.getPackageWidth()));
        setStringCell(row, cols.colPkgHeightMm, cmToMm(dto.getPackageHeight()));
        setStringCell(row, cols.colPkgLengthMm, cmToMm(dto.getPackageLength()));
        // Вес уже хранится в граммах
        setStringCell(row, cols.colPkgWeightG, n(dto.getPackageWeight()));

        // Композитное поле: Размер упаковки (Д x Ш x В), см
        String sizeCm = composeSizeCm(dto.getPackageLength(), dto.getPackageWidth(), dto.getPackageHeight());
        setStringCell(row, cols.colSizeCm, sizeCm);
    }

    private void setStringCell(Row row, int colIdx, String value) {
        if (colIdx < 0) return;
        Cell cell = row.getCell(colIdx);
        if (cell == null) cell = row.createCell(colIdx);
        cell.setCellValue(n(value));
    }

    private String cmToMm(String cmStr) {
        Double cm = parseDouble(cmStr);
        if (cm == null) return "";
        long mm = Math.round(cm * 10.0);
        return String.valueOf(mm);
    }

    private String kgToG(String kgStr) {
        Double kg = parseDouble(kgStr);
        if (kg == null) return "";
        long g = Math.round(kg * 1000.0);
        return String.valueOf(g);
    }

    private String composeSizeCm(String lenCm, String widCm, String heiCm) {
        String l = safeNum(lenCm);
        String w = safeNum(widCm);
        String h = safeNum(heiCm);
        if (l.isEmpty() && w.isEmpty() && h.isEmpty()) return "";
        return String.format("%sх%sх%s", l, w, h);
    }

    private String safeNum(String s) {
        if (s == null || s.isBlank()) return "";
        // нормализуем запятую к точке
        return s.replace(',', '.');
    }

    private Double parseDouble(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            String norm = s.replace(',', '.').trim();
            return Double.parseDouble(norm);
        } catch (Exception e) {
            return null;
        }
    }
} 