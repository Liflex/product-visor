package ru.dmitartur.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.dmitartur.dto.MarketDto;
import ru.dmitartur.service.MarketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
public class MarketController {
    private static final Logger logger = LoggerFactory.getLogger(MarketController.class);

    private final MarketService service;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SneakyThrows
    public ResponseEntity<MarketDto> save(@RequestParam("marketData") String marketData,
                                          @RequestParam(value = "image", required = false) MultipartFile image) {
        logger.info("🔄 Creating new market");
        MarketDto marketDto = objectMapper.readValue(marketData, MarketDto.class);
        if (image != null && !image.isEmpty()) {
            try {
                byte[] imageBytes = image.getBytes();
                marketDto.setImage(imageBytes);
                logger.debug("🖼️ Image stored in database: {} bytes", imageBytes.length);
            } catch (IOException e) {
                logger.error("❌ Failed to read image file", e);
                throw new RuntimeException("Failed to process image", e);
            }
        }
        MarketDto saved = service.saveDto(marketDto);
        logger.info("✅ Market created successfully: id={}, name={}", saved.getId(), saved.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, path = "/{id}")
    @SneakyThrows
    public ResponseEntity<MarketDto> update(@RequestParam("marketData") String marketData,
                                            @RequestParam(value = "image", required = false) MultipartFile image,
                                            @PathVariable Long id) {
        logger.info("🔄 Updating market: id={}", id);
        MarketDto marketDto = objectMapper.readValue(marketData, MarketDto.class);
        marketDto.setId(id);
        if (image != null && !image.isEmpty()) {
            try {
                byte[] imageBytes = image.getBytes();
                marketDto.setImage(imageBytes);
                logger.debug("🖼️ Image stored in database: {} bytes", imageBytes.length);
            } catch (IOException e) {
                logger.error("❌ Failed to read image file", e);
                throw new RuntimeException("Failed to process image", e);
            }
        }
        MarketDto updated = service.saveDto(marketDto);
        logger.info("✅ Market updated successfully: id={}, name={}", updated.getId(), updated.getName());
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<MarketDto>> findAll() {
        logger.info("📋 Fetching all markets");
        List<MarketDto> markets = service.findAllDto();
        logger.info("✅ Found {} markets", markets.size());
        return ResponseEntity.ok(markets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarketDto> findById(@PathVariable Long id) {
        logger.info("🔍 Fetching market by ID: {}", id);
        MarketDto market = service.findByIdDto(id);
        if (market != null) {
            logger.info("✅ Market found: id={}, name={}", id, market.getName());
            return ResponseEntity.ok(market);
        } else {
            logger.warn("❌ Market not found: id={}", id);
            return ResponseEntity.ok(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        logger.info("🗑️ Deleting market: id={}", id);
        service.deleteById(id);
        logger.info("✅ Market deleted successfully: id={}", id);
        return ResponseEntity.ok().build();
    }
} 