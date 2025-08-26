package ru.dmitartur.client.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.dmitartur.common.utils.JwtUtil;
import ru.dmitartur.client.entity.Company;
import ru.dmitartur.client.service.CompanyService;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<List<Company>> listMine() {
        return ResponseEntity.ok(companyService.listMyCompanies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(companyService.getCompany(id));
    }

    @PostMapping
    public ResponseEntity<Company> create(@Valid @RequestBody Company req) {
        Company saved = companyService.createCompany(req);
        return ResponseEntity.created(URI.create("/api/companies/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> update(@PathVariable("id") UUID id, @Valid @RequestBody Company req) {
        return ResponseEntity.ok(companyService.updateCompany(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> currentCompany() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("companyId", JwtUtil.resolveEffectiveCompanyId().orElse(null));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@PathVariable("id") UUID id, @RequestParam("file") MultipartFile file) {
        try {
            companyService.uploadAvatar(id, file.getBytes());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Avatar uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}


