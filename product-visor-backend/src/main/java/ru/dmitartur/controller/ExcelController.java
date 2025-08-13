package ru.dmitartur.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.dmitartur.excel.ExcelExportService;
import ru.dmitartur.excel.ExcelTemplateScanner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelExportService excelExportService;
    private final ExcelTemplateScanner excelTemplateScanner;

    @PostMapping(value = "/lenses/export", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> exportLenses(@RequestBody ExportRequest request) {
        byte[] bytes = excelExportService.exportLensExcel(request.getProductIds());
        String filename = request.getProductIds() != null && request.getProductIds().size() == 1
                ? "lenses_" + request.getProductIds().get(0) + ".xlsx"
                : "lenses_export.xlsx";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/template/scan")
    public ResponseEntity<ExcelTemplateScanner.ScanResponse> scanTemplate() {
        return ResponseEntity.ok(excelTemplateScanner.scanFormTemplate());
    }

    @Data
    public static class ExportRequest {
        private List<Long> productIds;
    }
} 