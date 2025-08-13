package org.example.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class OzonLensesFormFiller {

    public byte[] fillFromTemplate(List<LensExcelDto> dtos) {
        try (InputStream in = new ClassPathResource("form.xlsx").getInputStream();
             Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheet("Шаблон");
            if (sheet == null) sheet = wb.getSheetAt(0);

            ConfigIndices cfg = readConfigIndices(wb);
            ColumnMap cols = buildColumnMap(sheet, cfg.headerRowIndex);

            int rowIndex = cfg.firstDataRowIndex;
            for (int i = 0; i < dtos.size(); i++) {
                writeRow(sheet, rowIndex + i, cols, dtos.get(i));
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to fill template", e);
        }
    }

    private static class ConfigIndices {
        int headerRowIndex = 1;      // default 0-based row index of headers
        int firstDataRowIndex = 4;   // default 0-based row index for first data row
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
        int colNds = -1;
        int colRasrochka = -1;
        int colBonusForOtziv = -1;
        int colType = -1;
        int colHashtag = -1;
        int colExpiredDateInDays = -1;
        int colDaysForSwitch = -1;
        int colSex = -1;
        int colRezim = -1;
        int colComplect = -1;
        int colCountry = -1;
        int colBrand = -1;
        int colGuaruantie = -1;
        int colVlaznost = -1;
        int colAnnotation = -1;
        int colSizeCm = -1; // Размер упаковки (Длина х Ширина х Высота), см
        int colModelName = -1; // Размер упаковки (Длина х Ширина х Высота), см
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
                case "НДС, %*" -> map.colNds = c;
                case "Рассрочка" -> map.colRasrochka = c;
                case "Баллы за отзывы" -> map.colBonusForOtziv = c;
                case "Тип*" -> map.colType = c;
                case "#Хештеги" -> map.colHashtag = c;
                case "Срок годности в днях" -> map.colExpiredDateInDays = c;
                case "Дней до замены" -> map.colDaysForSwitch = c;
                case "Пол" -> map.colSex = c;
                case "Режим ношения" -> map.colRezim = c;
                case "Комплектация" -> map.colComplect = c;
                case "Страна-изготовитель" -> map.colCountry = c;
                case "Бренд*" -> map.colBrand = c;
                case "Гарантийный срок" -> map.colGuaruantie = c;
                case "Влагосодержание, %" -> map.colVlaznost = c;
                case "Аннотация" -> map.colAnnotation = c;
                case "Название модели (для объединения в одну карточку)*" -> map.colModelName = c;
                default -> {}
            }
        }
        return map;
    }

    private void writeRow(Sheet sheet, int rowIdx, ColumnMap cols, LensExcelDto dto) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) row = sheet.createRow(rowIdx);
        setStringCell(row, cols.colArticle, dto.getArticle());
        setStringCell(row, cols.colName, formatProductName(dto.getProductName(), formatDioptries(dto.getAttrDioptries())));
        setStringCell(row, cols.colPrice, dto.getPrice());
        setStringCell(row, cols.colBarcode, dto.getBarcode());
        setStringCell(row, cols.colQtyInPack, dto.getPackageQuantityInPack());
        setStringCell(row, cols.colColor, dto.getAttrColor());
        setStringCell(row, cols.colDioptries, formatDioptries(dto.getAttrDioptries()));
        setStringCell(row, cols.colCurvature, dto.getAttrCurvature());
        setStringCell(row, cols.colDiameterMm, dto.getAttrDiameter());

        // Конвертации: см -> мм, вес уже в граммах
        setStringCell(row, cols.colPkgWidthMm, cmToMm(dto.getPackageWidth()));
        setStringCell(row, cols.colPkgHeightMm, cmToMm(dto.getPackageHeight()));
        setStringCell(row, cols.colPkgLengthMm, cmToMm(dto.getPackageLength()));
        setStringCell(row, cols.colPkgWeightG, n(dto.getPackageWeight()));

        // Композитное поле: ДхШхВ, см
        String sizeCm = composeSizeCm(dto.getPackageLength(), dto.getPackageWidth(), dto.getPackageHeight());
        setStringCell(row, cols.colSizeCm, sizeCm);
        //Иные автоматом
        setStringCell(row, cols.colNds, "Не облагается");
        setStringCell(row, cols.colRasrochka, "Да");
        setStringCell(row, cols.colBonusForOtziv, "Да");
        setStringCell(row, cols.colType, "Цветные контактные линзы");
        setStringCell(row, cols.colHashtag, "#линзы #цветныелинзы #контактныелинзы #красота #косплей");
        setStringCell(row, cols.colExpiredDateInDays, "1825");
        setStringCell(row, cols.colDaysForSwitch, "12 месяцев");
        setStringCell(row, cols.colSex, "Мужской;Женский;Мальчики;Девочки");
        setStringCell(row, cols.colRezim, "Дневной");
        setStringCell(row, cols.colComplect, "1 пара линз, кейс для хранения");
        setStringCell(row, cols.colCountry, "Страна-изготовитель");
        setStringCell(row, cols.colBrand, "Нет бренда");
        setStringCell(row, cols.colGuaruantie, "0");
        setStringCell(row, cols.colVlaznost, "38");
        setStringCell(row, cols.colModelName, dto.getArticle());
        setStringCell(row, cols.colAnnotation, "Цветные контактные линзы для глаз Lalens - это уникальный продукт, который поможет вам выделиться из толпы. \n" +
                "\n" +
                "Линзы Lalens  - это идеальный выбор для тех, кто хочет изменить свой образ и добавить яркости своим глазам. Они подходят как для мужчин, так и для женщин любого возраста и пола.\n" +
                "\n" +
                "Вы можете быть уверены, что покупая эти линзы, вы получаете только лучшее!");
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

    private String composeSizeCm(String lenCm, String widCm, String heiCm) {
        String l = safeNum(lenCm);
        String w = safeNum(widCm);
        String h = safeNum(heiCm);
        if (l.isEmpty() && w.isEmpty() && h.isEmpty()) return "";
        return String.format("%sх%sх%s", l, w, h);
    }

    private String safeNum(String s) {
        if (s == null || s.isBlank()) return "";
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

    private String n(String s) { return s == null ? "" : s; }

    private String formatProductName(String productName, String formatedDioptries) {
        String formatedDioptriesCorrectedForName = formatedDioptries == null || formatedDioptries.isBlank() || formatedDioptries.equals("0") ? "0.00" : formatedDioptries;

        return "Цветные контактные линзы для глаз Lalens, " + productName +
                ", 12 месяцев, "
                + formatedDioptriesCorrectedForName;
    }

    // Спец-формат для Оптической силы: 0 -> пусто, иначе 2 знака после точки с точкой
    private String formatDioptries(String input) {
        if (input == null || input.isBlank() || input.equals("0")) return "";
        String[] parts = input.split(";\\s*");
        List<String> out = new ArrayList<>();
        DecimalFormatSymbols sym = new DecimalFormatSymbols();
        sym.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", sym);
        for (String p : parts) {
            Double val = parseDouble(p);
            if (val == null) continue;
            if (Math.abs(val) < 1e-9) continue; // 0 -> пусто
            out.add(df.format(val));
        }
        return String.join("; ", out);
    }
} 