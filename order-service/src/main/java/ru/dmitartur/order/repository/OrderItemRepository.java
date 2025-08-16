package ru.dmitartur.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.dmitartur.order.entity.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Найти товары с ненайденными продуктами (productId = null)
     */
    Page<OrderItem> findByProductIdIsNull(Pageable pageable);
    
    /**
     * Подсчитать количество товаров с ненайденными продуктами
     */
    long countByProductIdIsNull();
    
    /**
     * Найти уникальные SKU товаров с ненайденными продуктами
     */
    @Query("SELECT DISTINCT oi.sku FROM OrderItem oi WHERE oi.productId IS NULL AND oi.sku IS NOT NULL")
    List<String> findDistinctSkuByProductIdIsNull();
    
    /**
     * Подсчитать количество уникальных SKU
     */
    @Query("SELECT COUNT(DISTINCT oi.sku) FROM OrderItem oi WHERE oi.sku IS NOT NULL")
    long countDistinctSku();
    
    /**
     * Подсчитать количество уникальных SKU с ненайденными продуктами
     */
    @Query("SELECT COUNT(DISTINCT oi.sku) FROM OrderItem oi WHERE oi.productId IS NULL AND oi.sku IS NOT NULL")
    long countDistinctSkuByProductIdIsNull();
}
