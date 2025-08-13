package ru.dmitartur.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.dmitartur.dto.ProductDto;
import ru.dmitartur.dto.ProductUploadRequest;
import ru.dmitartur.entity.Product;
import ru.dmitartur.mapper.ProductMapper;
import ru.dmitartur.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService service;
    private final ObjectMapper objectMapper;
    private final ProductMapper mapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SneakyThrows
    public ResponseEntity<ProductDto> save(@ModelAttribute ProductUploadRequest productUploadRequest) {
        logger.info("🔄 Creating new product");

        ProductDto productDto = objectMapper.readValue(productUploadRequest.getProductData(), ProductDto.class);
        logger.debug("📝 Product data parsed: name={}, barcode={}", productDto.getName(), productDto.getBarcode());

        // Сохраняем изображение в базу данных
        if (productUploadRequest.getImage() != null && !productUploadRequest.getImage().isEmpty()) {
            try {
                byte[] imageBytes = productUploadRequest.getImage().getBytes();
                productDto.setImage(imageBytes);
                logger.debug("🖼️ Image stored in database: {} bytes", imageBytes.length);
            } catch (IOException e) {
                logger.error("❌ Failed to read image file", e);
                throw new RuntimeException("Failed to process image", e);
            }
        }

        Product entity = mapper.toEntity(productDto);

        Product savedProduct = service.save(entity);
        logger.info("✅ Product created successfully: id={}, name={}", savedProduct.getId(), savedProduct.getName());

        return ResponseEntity.ok(mapper.toDto(savedProduct));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, path = "/{id}")
    @SneakyThrows
    public ResponseEntity<ProductDto> update(@ModelAttribute ProductUploadRequest productUploadRequest, @PathVariable Long id) {
        logger.info("🔄 Updating product: id={}", id);

        ProductDto productDto = objectMapper.readValue(productUploadRequest.getProductData(), ProductDto.class);
        productDto.setId(id);
        logger.debug("📝 Product data parsed: name={}, barcode={}", productDto.getName(), productDto.getBarcode());

        // Сохраняем изображение в базу данных
        if (productUploadRequest.getImage() != null && !productUploadRequest.getImage().isEmpty()) {
            try {
                byte[] imageBytes = productUploadRequest.getImage().getBytes();
                productDto.setImage(imageBytes);
                logger.debug("🖼️ Image stored in database: {} bytes", imageBytes.length);
            } catch (IOException e) {
                logger.error("❌ Failed to read image file", e);
                throw new RuntimeException("Failed to process image", e);
            }
        } else {
            logger.debug("🖼️ No image provided");
        }

        Product entity = mapper.toEntity(productDto);
        Product updatedProduct = service.update(entity);
        logger.info("✅ Product updated successfully: id={}, name={}", updatedProduct.getId(), updatedProduct.getName());

        return ResponseEntity.ok(mapper.toDto(updatedProduct));
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> findAll(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        logger.info("📋 Fetching products with pagination: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Product> productPage = service.findAll(pageable);
        Page<ProductDto> dtos = productPage.map(mapper::toDto);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/barcode")
    public ResponseEntity<ProductDto> findByBarcode(@RequestParam String barcode) {
        logger.info("🔍 Searching product by barcode: {}", barcode);

        Optional<Product> product = service.findByBarcode(barcode);

        logger.info("✅ Product found by barcode: {} -> Product ID: {}", barcode, product.get().getId());
        return ResponseEntity.ok(mapper.toDto(product.get()));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> search(
            @RequestParam("q") String q,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        logger.info("🔎 Searching products: q='{}', page={}, size={}", q, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = service.search(q, pageable);
        List<ProductDto> dtos = productPage.getContent().stream().map(mapper::toDto).toList();
        Page<ProductDto> dtoPage = new PageImpl<>(dtos, pageable, productPage.getTotalElements());
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> findById(@PathVariable Long id) {
        logger.info("🔍 Fetching product by ID: {}", id);

        Optional<Product> product = service.findById(id);

        if (product.isPresent()) {
            logger.info("✅ Product found: id={}, name={}", id, product.get().getName());
            return ResponseEntity.ok(mapper.toDto(product.get()));
        } else {
            logger.warn("❌ Product not found: id={}", id);
            return ResponseEntity.ok(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        logger.info("🗑️ Deleting product: id={}", id);
            service.deleteById(id);
            logger.info("✅ Product deleted successfully: id={}", id);
            return ResponseEntity.ok().build();
    }
}
