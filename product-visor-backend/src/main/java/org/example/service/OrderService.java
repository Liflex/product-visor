package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.CreateOrderRequest;
import org.example.dto.OrderDto;
import org.example.entity.Order;
import org.example.entity.Product;
import org.example.entity.Market;
import org.example.mapper.OrderMapper;
import org.example.repository.OrderRepository;
import org.example.repository.ProductRepository;
import org.example.repository.MarketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MarketRepository marketRepository;
    private final OrderMapper orderMapper;
    
    public OrderDto createOrder(CreateOrderRequest request) {
        Product product = productRepository.findById(request.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        Market market = marketRepository.findById(request.getMarket().getId())
                .orElseThrow(() -> new RuntimeException("Market not found"));
        
        Order order = new Order();
        order.setProduct(product);
        order.setMarket(market);
        order.setOrderBarcode(request.getOrderBarcode());
        order.setPrice(request.getPrice());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(org.example.entity.OrderStatus.PURCHASED);
        
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }
    
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }
    
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return orderMapper.toDto(order);
    }
    
    public List<OrderDto> getOrdersByProductId(Long productId) {
        return orderRepository.findByProductId(productId).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }
    
    public List<OrderDto> getOrdersByMarketId(Long marketId) {
        return orderRepository.findByMarketId(marketId).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }
    
    public OrderDto getOrderByBarcode(String orderBarcode) {
        List<Order> orders = orderRepository.findByOrderBarcode(orderBarcode);
        if (orders.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        return orderMapper.toDto(orders.get(0));
    }
} 