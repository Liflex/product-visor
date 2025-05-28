package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.CategoryDto;
import org.example.mapper.CategoryMapper;
import org.example.service.CategoryService;
import org.example.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
public class ImageController {
    private final FileStorageService fileStorageService;

    /**
     * Получение изображения по имени файла
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Resource file = fileStorageService.loadFile(filename);

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                    .contentType(MediaType.IMAGE_JPEG) // или определять динамически
                    .body(file);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }
    }
}
