package ru.dmitartur.order.repository;

import ru.dmitartur.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByPostingNumber(String postingNumber);
}


