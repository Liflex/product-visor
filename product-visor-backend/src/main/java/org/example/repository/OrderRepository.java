package org.example.repository;

import org.example.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByProductId(Long productId);
    List<Order> findByMarketId(Long marketId);
    List<Order> findByOrderBarcode(String orderBarcode);
} 