package ru.dmitartur.client.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.dmitartur.client.service.CompanyService;

import java.util.UUID;

@RestController
@RequestMapping("/api/companies/{companyId}/avatar")
@RequiredArgsConstructor
public class CompanyAvatarController {
    private final CompanyService companyService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(@PathVariable("companyId") UUID companyId, @RequestPart("file") MultipartFile file) throws Exception {
        companyService.uploadAvatar(companyId, file.getBytes());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable("companyId") UUID companyId) {
        byte[] avatar = companyService.getAvatar(companyId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(avatar);
    }
}



