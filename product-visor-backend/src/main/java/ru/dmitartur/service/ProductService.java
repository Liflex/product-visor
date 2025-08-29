package ru.dmitartur.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.dmitartur.entity.Product;
import ru.dmitartur.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.common.utils.JwtUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    // private final ProductHistoryInterceptor productHistoryInterceptor; // Quantity tracking moved to ProductStock level

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
            // Проставляем владельца и компанию из контекста
            try {
                entity.setOwnerUserId(JwtUtil.getRequiredOwnerId());
            } catch (IllegalStateException e) {
                logger.error("❌ Missing or invalid user_id in JWT: {}", e.getMessage(), e);
                throw e;
            }
            JwtUtil.resolveEffectiveCompanyId().ifPresent(cid -> {
                try { entity.setCompanyId(java.util.UUID.fromString(cid)); } catch (Exception ignored) {}
            });
        } else {
            logger.info("💾 Updating product: id={}, name={}", entity.getId(), entity.getName());
        }
        
        Product savedProduct = productRepository.save(entity);
        logger.info("✅ Product saved successfully: id={}, name={}", savedProduct.getId(), savedProduct.getName());
        return savedProduct;
    }

    public Product update(Product entity) {
        logger.info("🔄 Updating product: id={}, name={}", 
            entity.getId(), entity.getName());
        
        try {
            Product savedProduct = productRepository.save(entity);
            logger.info("✅ Product updated successfully: id={}, name={}", 
                savedProduct.getId(), savedProduct.getName());
            return savedProduct;
        } catch (Exception e) {
            logger.error("❌ Error updating product: id={}, error={}", entity.getId(), e.getMessage(), e);
            throw e;
        }
    }

    // Количество на уровне Product больше не используется; остатки — в ProductStock

    public List<Product> findAll() {
        logger.debug("📋 Fetching all products for current owner");
        var ownerIdOpt = JwtUtil.getCurrentId();
        if (ownerIdOpt.isEmpty()) return List.of();
        UUID ownerId = JwtUtil.getRequiredOwnerId();
        return productRepository.findByOwnerUserId(ownerId, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    public Page<Product> findAll(Pageable pageable) {
        logger.debug("📋 Fetching products page for current owner: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        var ownerIdOpt = JwtUtil.getCurrentId();
        if (ownerIdOpt.isEmpty()) return Page.empty(pageable);
        UUID ownerId = JwtUtil.getRequiredOwnerId();
        return productRepository.findByOwnerUserId(ownerId, pageable);
    }

    public Page<Product> findAllByCompanyAndOwner(UUID companyId, UUID ownerId, Pageable pageable) {
        logger.debug("📋 Fetching products for company: {} and owner: {}, page={}, size={}", 
                companyId, ownerId, pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findByCompanyIdAndOwnerUserId(companyId, ownerId, pageable);
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
        
        // Получаем текущего пользователя
        UUID ownerUserId = JwtUtil.getRequiredOwnerId();
        
        // Определяем лимит для поиска (больше чем размер страницы для лучшего покрытия)
        int searchLimit = Math.max(pageable.getPageSize() * 3, 100);
        
        // Пытаемся получить companyId из контекста
        var companyIdOpt = JwtUtil.resolveEffectiveCompanyId();
        
        if (companyIdOpt.isPresent()) {
            try {
                UUID companyId = UUID.fromString(companyIdOpt.get());
                logger.debug("🔎 Searching products for company: {} and user: {}", companyId, ownerUserId);
                return productRepository.searchFullTextByCompany(query, ownerUserId, companyId, searchLimit, pageable);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid company ID format: {}, falling back to user-only search", companyIdOpt.get());
            }
        }
        
        // Поиск только по пользователю
        logger.debug("🔎 Searching products for user: {}", ownerUserId);
        return productRepository.searchFullText(query, ownerUserId, searchLimit, pageable);
    }

    /**
     * Получить товары по списку ID
     */
    public List<Product> getProductsByIds(List<Long> productIds) {
        logger.debug("🔍 Fetching products by IDs: {}", productIds);
        List<Product> products = productRepository.findAllById(productIds);
        logger.info("✅ Found {} products by IDs", products.size());
        return products;
    }

    /**
     * Проверить, что товар принадлежит текущему пользователю
     */
    public Product validateProductOwnership(Long productId) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));
        
        if (!product.getOwnerUserId().equals(userId)) {
            throw new RuntimeException("Товар не принадлежит пользователю");
        }
        
        return product;
    }

}
