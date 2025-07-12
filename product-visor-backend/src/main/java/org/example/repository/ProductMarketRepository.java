package org.example.repository;

import org.example.entity.ProductMarket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductMarketRepository extends JpaRepository<ProductMarket, Long> {
    List<ProductMarket> findByProductId(Long productId);
    List<ProductMarket> findByMarketId(Long marketId);
} 