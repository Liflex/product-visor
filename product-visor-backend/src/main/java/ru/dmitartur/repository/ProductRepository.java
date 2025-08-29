package ru.dmitartur.repository;

import ru.dmitartur.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {
        "productStocks",
        "productStocks.warehouses",
        "productAttributeValues",
        "productAttributeValues.attribute",
        "productMarkets",
        "productMarkets.market",
        "category"
    })
    Optional<Product> findById(Long id);
    
    @EntityGraph(attributePaths = {
        "productStocks",
        "productStocks.warehouses",
        "productAttributeValues",
        "productAttributeValues.attribute",
        "productMarkets",
        "productMarkets.market",
        "category"
    })
    Optional<Product> findByBarcode(String barcode);
    
    @EntityGraph(attributePaths = {
        "productStocks",
        "productStocks.warehouses",
        "productAttributeValues",
        "productAttributeValues.attribute",
        "productMarkets",
        "productMarkets.market",
        "category"
    })
    Optional<Product> findByArticle(String article);
    
    Page<Product> findByOwnerUserId(UUID ownerUserId, Pageable pageable);
    Page<Product> findByCompanyIdAndOwnerUserId(UUID companyId, UUID ownerUserId, Pageable pageable);

    @Query(value = "SELECT p.* FROM visor.product p " +
            "JOIN visor.search_products(:q, :limit) sp ON sp.product_id = p.id " +
            "WHERE p.owner_user_id = :ownerUserId " +
            "ORDER BY p.id",
           countQuery = "SELECT COUNT(*) FROM visor.product p " +
                   "JOIN visor.search_products(:q, :limit) sp ON sp.product_id = p.id " +
                   "WHERE p.owner_user_id = :ownerUserId",
           nativeQuery = true)
    Page<Product> searchFullText(@Param("q") String query, 
                                @Param("ownerUserId") UUID ownerUserId,
                                @Param("limit") int limit,
                                Pageable pageable);

    /**
     * Поиск товаров с учетом компании и владельца
     */
    @Query(value = "SELECT p.* FROM visor.product p " +
            "JOIN visor.search_products(:q, :limit) sp ON sp.product_id = p.id " +
            "WHERE p.owner_user_id = :ownerUserId AND p.company_id = :companyId " +
            "ORDER BY p.id",
           countQuery = "SELECT COUNT(*) FROM visor.product p " +
                   "JOIN visor.search_products(:q, :limit) sp ON sp.product_id = p.id " +
                   "WHERE p.owner_user_id = :ownerUserId AND p.company_id = :companyId",
           nativeQuery = true)
    Page<Product> searchFullTextByCompany(@Param("q") String query, 
                                         @Param("ownerUserId") UUID ownerUserId,
                                         @Param("companyId") UUID companyId,
                                         @Param("limit") int limit,
                                         Pageable pageable);
}
