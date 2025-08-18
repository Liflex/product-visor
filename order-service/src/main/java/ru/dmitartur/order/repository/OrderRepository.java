package ru.dmitartur.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.common.enums.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByPostingNumber(String postingNumber);
    Page<Order> findByMarket(Market market, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:dateFrom IS NULL OR o.ozonCreatedAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR o.ozonCreatedAt <= :dateTo)")
    Page<Order> findByFilters(@Param("status") String status, 
                             @Param("dateFrom") ZonedDateTime dateFrom, 
                             @Param("dateTo") ZonedDateTime dateTo, 
                             Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE " +
           "o.market = :market AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:dateFrom IS NULL OR o.ozonCreatedAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR o.ozonCreatedAt <= :dateTo)")
    Page<Order> findByMarketAndFilters(@Param("market") Market market,
                                      @Param("status") String status, 
                                      @Param("dateFrom") ZonedDateTime dateFrom, 
                                      @Param("dateTo") ZonedDateTime dateTo, 
                                      Pageable pageable);
}


