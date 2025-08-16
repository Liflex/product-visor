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
    private final MarketService marketService;
    private final ProductMarketService productMarketService;
    private final ProductMapper productMapper;
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
        return productRepository.save(entity);
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
    
    /**
     * Обновить количество товара
     */
    public boolean updateQuantity(Long productId, int quantityChange) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                int oldQuantity = product.getQuantity();
                int newQuantity = Math.max(0, oldQuantity + quantityChange);
                product.setQuantity(newQuantity);
                
                // Используем общий метод update для сохранения
                update(product);
                
                // Отслеживаем изменение
                productHistoryInterceptor.trackQuantityChange(product, oldQuantity, newQuantity);
                
                logger.info("✅ Updated product quantity: productId={}, change={}, newQuantity={}", 
                        productId, quantityChange, newQuantity);
                
                return true;
            } else {
                logger.warn("❌ Product not found for quantity update: productId={}", productId);
                return false;
            }
        } catch (Exception e) {
            logger.error("❌ Error updating product quantity: productId={}, error={}", productId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Обновить количество товара по артикулу
     */
    public boolean updateQuantityByArticle(String article, int quantityChange) {
        try {
            Optional<Product> productOpt = productRepository.findByArticle(article);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                return updateQuantity(product.getId(), quantityChange);
            } else {
                logger.warn("❌ Product not found by article for quantity update: article={}", article);
                return false;
            }
        } catch (Exception e) {
            logger.error("❌ Error updating product quantity by article: article={}, error={}", article, e.getMessage());
            return false;
        }
    }
}
