package ru.dmitartur.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.dmitartur.entity.Product;
import ru.dmitartur.mapper.ProductMapper;
import ru.dmitartur.repository.ProductRepository;
import ru.dmitartur.interceptor.ProductHistoryInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    private final ProductHistoryInterceptor productHistoryInterceptor;

    /**
     * Генерирует уникальный 12-значный артикул
     */
    private String generateArticle() {
        java.util.Random random = new java.util.Random();
        String article;
        do {
            long number = 100000000000L + Math.abs(random.nextLong() % 900000000000L);
            article = String.valueOf(number);
        } while (productRepository.findByArticle(article).isPresent());
        
        logger.debug("Generated unique article: {}", article);
        return article;
    }

    public Product save(Product entity) {
        if (entity.getId() == null) {
            logger.info("💾 Saving new product: name={}", entity.getName());
            if (entity.getArticle() == null || entity.getArticle().trim().isEmpty()) {
                entity.setArticle(generateArticle());
                logger.info("Generated article for new product: {}", entity.getArticle());
            }
        } else {
            logger.info("💾 Updating product: id={}, name={}", entity.getId(), entity.getName());
        }
        
        Product savedProduct = productRepository.save(entity);
        logger.info("✅ Product saved successfully: id={}, name={}", savedProduct.getId(), savedProduct.getName());
        return savedProduct;
    }

    public Product update(Product entity) {
        logger.info("🔄 Updating product: id={}, name={}, quantity={}", 
            entity.getId(), entity.getName(), entity.getQuantity());
        
        try {
            Product savedProduct = productRepository.save(entity);
            logger.info("✅ Product updated successfully: id={}, name={}, quantity={}", 
                savedProduct.getId(), savedProduct.getName(), savedProduct.getQuantity());
            return savedProduct;
        } catch (Exception e) {
            logger.error("❌ Error updating product: id={}, error={}", entity.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Compute new quantity based on delta (does not persist)
     */
    public int computeQuantityWithDelta(int currentQuantity, int quantityDelta) {
        int base = Math.max(0, currentQuantity);
        int newQuantity = base + quantityDelta;
        return Math.max(0, newQuantity);
    }

    public void trackQuantityChange(Product product, int oldQuantity, int newQuantity) {
        productHistoryInterceptor.trackQuantityChange(product, oldQuantity, newQuantity);
    }

    public List<Product> findAll() {
        logger.debug("📋 Fetching all products from database");
        List<Product> products = productRepository.findAll();
        logger.debug("✅ Found {} products", products.size());
        return products;
    }

    public Page<Product> findAll(Pageable pageable) {
        logger.debug("📋 Fetching products page: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> page = productRepository.findAll(pageable);
        logger.debug("✅ Page loaded: totalElements={}, totalPages={}", page.getTotalElements(), page.getTotalPages());
        return page;
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

    public Optional<Product> findByArticle(String article) {
        logger.debug("🔍 Searching product by article: {}", article);
        Optional<Product> product = productRepository.findByArticle(article);
        
        if (product.isPresent()) {
            logger.debug("✅ Product found by article: {} -> id={}", article, product.get().getId());
        } else {
            logger.debug("❌ Product not found by article: {}", article);
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

    public Page<Product> search(String query, Pageable pageable) {
        logger.debug("🔎 Searching products by query: '{}'", query);
        return productRepository.searchFullText(query, pageable);
    }

}
