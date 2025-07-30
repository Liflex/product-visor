package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.CreateOrderRequest;
import org.example.dto.OrderDto;
import org.example.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody CreateOrderRequest request) {
        OrderDto order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<OrderDto>> getOrdersByProductId(@PathVariable Long productId) {
        List<OrderDto> orders = orderService.getOrdersByProductId(productId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/market/{marketId}")
    public ResponseEntity<List<OrderDto>> getOrdersByMarketId(@PathVariable Long marketId) {
        List<OrderDto> orders = orderService.getOrdersByMarketId(marketId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/barcode/{orderBarcode}")
    public ResponseEntity<OrderDto> getOrderByBarcode(@PathVariable String orderBarcode) {
        OrderDto order = orderService.getOrderByBarcode(orderBarcode);
        return ResponseEntity.ok(order);
    }
} 