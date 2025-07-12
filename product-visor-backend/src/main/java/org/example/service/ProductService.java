package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.dto.ProductDto;
import org.example.entity.Category;
import org.example.entity.Market;
import org.example.entity.Product;
import org.example.entity.ProductMarket;
import org.example.mapper.ProductMapper;
import org.example.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    private final MarketService marketService;
    private final ProductMarketService productMarketService;
    private final ProductMapper productMapper;

    public Product save(Product entity) {
        if (entity.getId() == null) {
            logger.info("💾 Saving new product: name={}", entity.getName());
        } else {
            logger.info("💾 Updating product: id={}, name={}", entity.getId(), entity.getName());
        }
        
        Product savedProduct = productRepository.save(entity);
        logger.info("✅ Product saved successfully: id={}, name={}", savedProduct.getId(), savedProduct.getName());
        return savedProduct;
    }

    public Product update(Product entity) {
        return productRepository.save(entity);
    }

    public List<Product> findAll() {
        logger.debug("📋 Fetching all products from database");
        List<Product> products = productRepository.findAll();
        logger.debug("✅ Found {} products", products.size());
        return products;
    }

    public Optional<Product> findByBarcode(String barcode) {
        logger.debug("🔍 Searching product by barcode: {}", barcode);
        Optional<Product> product = productRepository.findByBarcode(barcode);
        
        if (product.isPresent()) {
            logger.debug("✅ Product found by barcode: {} -> id={}", barcode, product.get().getId());
        } else {
            logger.debug("❌ Product not found by barcode: {}", barcode);
        }
        
        return product;
    }

    @SneakyThrows
    public Optional<Product> findById(Long id) {
        logger.debug("🔍 Searching product by ID: {}", id);
        Optional<Product> product = productRepository.findById(id);
        
        if (product.isPresent()) {
            logger.debug("✅ Product found by ID: {} -> name={}", id, product.get().getName());
        } else {
            logger.debug("❌ Product not found by ID: {}", id);
        }
        
        return product;
    }

    public void deleteById(Long id) {
        logger.info("🗑️ Deleting product by ID: {}", id);
        
        if (!productRepository.existsById(id)) {
            logger.warn("❌ Product not found for deletion: id={}", id);
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        
        productRepository.deleteById(id);
        logger.info("✅ Product deleted successfully: id={}", id);
    }
}
