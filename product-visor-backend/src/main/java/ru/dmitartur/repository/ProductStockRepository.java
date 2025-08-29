package ru.dmitartur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dmitartur.entity.ProductStock;
import ru.dmitartur.common.enums.ProductStockType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, UUID> {
    
    /**
     * Найти остатки товара по всем складам пользователя
     */
    List<ProductStock> findByProductIdAndUserId(Long productId, UUID userId);
    
    /**
     * Найти остатки товара по всем складам пользователя с загрузкой складов
     */
    @Query("SELECT ps FROM ProductStock ps JOIN FETCH ps.warehouses WHERE ps.product.id = :productId AND ps.userId = :userId")
    List<ProductStock> findByProductIdAndUserIdWithWarehouses(@Param("productId") Long productId, @Param("userId") UUID userId);
    
    /**
     * Найти остатки товара на конкретном складе пользователя
     */
    @Query("SELECT ps FROM ProductStock ps JOIN FETCH ps.warehouses w " +
           "WHERE ps.product.id = :productId AND w.id = :warehouseId AND ps.userId = :userId")
    Optional<ProductStock> findByProductIdAndWarehouseIdAndUserId(@Param("productId") Long productId, 
                                                                 @Param("warehouseId") UUID warehouseId, 
                                                                 @Param("userId") UUID userId);
    
    /**
     * Найти остатки товара по типу
     */
    List<ProductStock> findByProductIdAndStockTypeAndUserId(Long productId, ProductStockType stockType, UUID userId);
    
    /**
     * Найти все остатки товара с загрузкой продукта и складов
     */
    @Query("SELECT ps FROM ProductStock ps JOIN FETCH ps.product JOIN FETCH ps.warehouses WHERE ps.product.id = :productId AND ps.userId = :userId")
    List<ProductStock> findByProductIdWithProduct(@Param("productId") Long productId, @Param("userId") UUID userId);
    
    /**
     * Найти остатки по списку продуктов и типу
     */
    @Query("SELECT ps FROM ProductStock ps JOIN FETCH ps.product WHERE ps.product.id IN :productIds AND ps.stockType = :stockType AND ps.userId = :userId")
    List<ProductStock> findByProductIdInAndStockTypeAndUserId(@Param("productIds") List<Long> productIds, 
                                                              @Param("stockType") ProductStockType stockType, 
                                                              @Param("userId") UUID userId);
    
    /**
     * Найти все остатки на складе пользователя
     */
    @Query("SELECT ps FROM ProductStock ps JOIN FETCH ps.warehouses w " +
           "WHERE w.id = :warehouseId AND ps.userId = :userId")
    List<ProductStock> findByWarehouseIdAndUserId(@Param("warehouseId") UUID warehouseId, @Param("userId") UUID userId);
    
    /**
     * Найти остатки товаров компании пользователя на домашнем складе
     */
    @Query("SELECT ps FROM ProductStock ps " +
           "JOIN ps.warehouses w " +
           "WHERE w.companyId = :companyId AND w.userId = :userId AND w.isHomeWarehouse = true")
    List<ProductStock> findByCompanyIdAndUserIdAndHomeWarehouse(@Param("companyId") UUID companyId, @Param("userId") UUID userId);
    
    /**
     * Найти остатки по типу склада (FBS/FBO)
     */
    @Query("SELECT ps FROM ProductStock ps " +
           "JOIN ps.warehouses w " +
           "WHERE w.warehouseType = :warehouseType AND ps.userId = :userId")
    List<ProductStock> findByWarehouseTypeAndUserId(@Param("warehouseType") String warehouseType, @Param("userId") UUID userId);
    
    /**
     * Найти остатки по маркетплейсу
     */
    @Query("SELECT ps FROM ProductStock ps " +
           "JOIN ps.warehouses w " +
           "WHERE w.marketplace = :marketplace AND ps.userId = :userId")
    List<ProductStock> findByMarketplaceAndUserId(@Param("marketplace") String marketplace, @Param("userId") UUID userId);
    
    /**
     * Обновить количество товара на складе
     */
    @Query("UPDATE ProductStock ps SET ps.quantity = :quantity, ps.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ps.product.id = :productId AND EXISTS (SELECT 1 FROM ps.warehouses w WHERE w.id = :warehouseId)")
    void updateQuantity(@Param("productId") Long productId, 
                       @Param("warehouseId") UUID warehouseId, 
                       @Param("quantity") Integer quantity);
    
    /**
     * Найти ProductStock по товару, складу и компании
     * Используется для обновления количества при обработке событий заказов
     */
    @Query("SELECT ps FROM ProductStock ps " +
           "JOIN ps.warehouses w " +
           "WHERE ps.product.id = :productId AND w.id = :warehouseId AND w.companyId = :companyId")
    Optional<ProductStock> findByProductIdAndWarehouseIdAndCompanyId(@Param("productId") Long productId, 
                                                                     @Param("warehouseId") UUID warehouseId, 
                                                                     @Param("companyId") UUID companyId);
    
    /**
     * Найти ProductStock по артикулу, складу и компании
     * Используется для обновления количества при обработке событий заказов
     */
    @Query("SELECT ps FROM ProductStock ps " +
           "JOIN ps.warehouses w " +
           "WHERE ps.product.article = :article AND w.externalWarehouseId = :warehouseId AND w.companyId = :companyId")
    Optional<ProductStock> findByArticleAndWarehouseIdAndCompanyId(@Param("article") String article, 
                                                                   @Param("warehouseId") String warehouseId,
                                                                   @Param("companyId") UUID companyId);
}
