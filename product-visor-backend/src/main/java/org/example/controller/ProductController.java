package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.dto.ProductDto;
import org.example.dto.ProductUploadRequest;
import org.example.mapper.ProductMapper;
import org.example.service.FileStorageService;
import org.example.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService;
    private final ProductMapper mapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SneakyThrows
    public ResponseEntity<ProductDto> save(@ModelAttribute ProductUploadRequest productUploadRequest) {
        ProductDto productDto = objectMapper.readValue(productUploadRequest.getProductData(), ProductDto.class);
        // Сохраняем изображение и получаем его URL
        String imageUrl = fileStorageService.storeFile(productUploadRequest.getImage());
        productDto.setImageUrl(imageUrl);

        service.save(mapper.toEntity(productDto));
        System.out.println("SSS");
        return ResponseEntity.ok(null);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, path = "/{id}")
    @SneakyThrows
    public ResponseEntity<ProductDto> update(@ModelAttribute ProductUploadRequest productUploadRequest, @PathVariable Long id) {
        ProductDto productDto = objectMapper.readValue(productUploadRequest.getProductData(), ProductDto.class);
        productDto.setId(id);
        // Сохраняем изображение и получаем его URL
        if(productUploadRequest.getImage() != null) {
            String imageUrl = fileStorageService.storeFile(productUploadRequest.getImage());
            productDto.setImageUrl(imageUrl);
        }
        service.save(mapper.toEntity(productDto));
        return ResponseEntity.ok(null);
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> findAll() {
        return ResponseEntity.ok(service.findAll().stream().map(mapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id).map(mapper::toDto).orElse(null));
    }
}
