package ru.dmitartur.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.repository.OrderRepository;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;

    @GetMapping
    public ResponseEntity<Page<Order>> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderRepository.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{postingNumber}")
    public ResponseEntity<Order> get(@PathVariable String postingNumber) {
        return orderRepository.findByPostingNumber(postingNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}


