package org.example.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelTemplateScanner {

    public ScanResponse scanFormTemplate() {
        List<CellInfo> cells = new ArrayList<>();
        List<PlaceholderInfo> placeholders = new ArrayList<>();
        try (InputStream in = new ClassPathResource("form.xlsx").getInputStream();
             Workbook wb = new XSSFWorkbook(in)) {
            for (int si = 0; si < wb.getNumberOfSheets(); si++) {
                Sheet sheet = wb.getSheetAt(si);
                for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        if (cell == null) continue;
                        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                            String text = cell.getStringCellValue();
                            if (text != null && !text.isBlank()) {
                                cells.add(new CellInfo(wb.getSheetName(si), r, c, text));
                                if (text.contains("${")) {
                                    placeholders.add(new PlaceholderInfo(wb.getSheetName(si), r, c, text));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan form.xlsx", e);
        }
        return new ScanResponse(cells, placeholders);
    }

    @Data
    @AllArgsConstructor
    public static class CellInfo {
        private String sheet;
        private int row;
        private int col;
        private String text;
    }

    @Data
    @AllArgsConstructor
    public static class PlaceholderInfo {
        private String sheet;
        private int row;
        private int col;
        private String text;
    }

    @Data
    @AllArgsConstructor
    public static class ScanResponse {
        private List<CellInfo> cells;
        private List<PlaceholderInfo> placeholders;
    }
} 