package ru.dmitartur.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dmitartur.entity.ProductHistory;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repository для работы с историей изменений продукта
 */
@Repository
public interface ProductHistoryRepository extends JpaRepository<ProductHistory, Long> {
    
    /**
     * Найти историю изменений по ID продукта
     */
    Page<ProductHistory> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    
    /**
     * Найти историю изменений по ID продукта и полю
     */
    List<ProductHistory> findByProductIdAndFieldNameOrderByCreatedAtDesc(Long productId, String fieldName);
    
    /**
     * Найти историю изменений по причине
     */
    Page<ProductHistory> findByChangeReasonOrderByCreatedAtDesc(String changeReason, Pageable pageable);
    
    /**
     * Найти историю изменений по системе-источнику
     */
    Page<ProductHistory> findBySourceSystemOrderByCreatedAtDesc(String sourceSystem, Pageable pageable);
    
    /**
     * Найти историю изменений по ID источника
     */
    List<ProductHistory> findBySourceIdOrderByCreatedAtDesc(String sourceId);
    
    /**
     * Найти историю изменений за период
     */
    @Query("SELECT ph FROM ProductHistory ph WHERE ph.createdAt BETWEEN :fromDate AND :toDate ORDER BY ph.createdAt DESC")
    Page<ProductHistory> findByDateRange(@Param("fromDate") OffsetDateTime fromDate, 
                                        @Param("toDate") OffsetDateTime toDate, 
                                        Pageable pageable);
    
    /**
     * Найти последнее изменение для продукта и поля
     */
    @Query("SELECT ph FROM ProductHistory ph WHERE ph.productId = :productId AND ph.fieldName = :fieldName " +
           "ORDER BY ph.createdAt DESC LIMIT 1")
    ProductHistory findLastChangeByProductAndField(@Param("productId") Long productId, 
                                                  @Param("fieldName") String fieldName);
    
    /**
     * Найти изменения по причине и периоду
     */
    @Query("SELECT ph FROM ProductHistory ph WHERE ph.changeReason = :changeReason " +
           "AND ph.createdAt BETWEEN :fromDate AND :toDate ORDER BY ph.createdAt DESC")
    List<ProductHistory> findByChangeReasonAndDateRange(@Param("changeReason") String changeReason,
                                                       @Param("fromDate") OffsetDateTime fromDate,
                                                       @Param("toDate") OffsetDateTime toDate);
}
