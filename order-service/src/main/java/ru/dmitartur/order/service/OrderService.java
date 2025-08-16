package ru.dmitartur.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.dmitartur.order.dto.OrderDto;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.mapper.OrderMapper;
import ru.dmitartur.order.repository.OrderRepository;
import ru.dmitartur.order.repository.OrderStatusHistoryRepository;
import ru.dmitartur.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.order.service.product.ProductService;
import ru.dmitartur.order.service.product.StockUpdateService;

import java.util.Optional;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderMapper orderMapper;
    private final ProductService productService;
    private final StockUpdateService stockUpdateService;
    private final OrderBatchProcessor orderBatchProcessor;

    /**
     * Получить все заказы с пагинацией
     */
    public Page<Order> findAll(Pageable pageable) {
        log.debug("📋 Fetching orders with pagination: page={}, size={}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        Page<Order> page = orderRepository.findAll(pageable);
        log.debug("✅ Orders page loaded: totalElements={}, totalPages={}", 
                 page.getTotalElements(), page.getTotalPages());
        return page;
    }

    /**
     * Получить заказы по маркету с пагинацией
     */
    public Page<Order> findByMarket(ru.dmitartur.common.enums.Market market, Pageable pageable) {
        log.debug("📋 Fetching orders by market: {} with pagination: page={}, size={}", 
                 market, pageable.getPageNumber(), pageable.getPageSize());
        Page<Order> page = orderRepository.findByMarket(market, pageable);
        log.debug("✅ Orders by market loaded: market={}, totalElements={}, totalPages={}", 
                 market, page.getTotalElements(), page.getTotalPages());
        return page;
    }

    /**
     * Найти заказ по номеру постинга
     */
    public Optional<Order> findByPostingNumber(String postingNumber) {
        log.debug("🔍 Searching order by posting number: {}", postingNumber);
        Optional<Order> order = orderRepository.findByPostingNumber(postingNumber);
        
        if (order.isPresent()) {
            log.debug("✅ Order found by posting number: {} -> id={}", postingNumber, order.get().getId());
        } else {
            log.debug("❌ Order not found by posting number: {}", postingNumber);
        }
        
        return order;
    }

    /**
     * Сохранить заказ
     */
    public Order save(Order order) {
        log.info("💾 Saving order: postingNumber={}", order.getPostingNumber());
        Order savedOrder = orderRepository.save(order);
        log.info("✅ Order saved successfully: id={}, postingNumber={}", 
                savedOrder.getId(), savedOrder.getPostingNumber());
        
        // Обновляем остатки через Kafka события
        stockUpdateService.updateStockForOrder(savedOrder);
        
        return savedOrder;
    }

    /**
     * Обновить заказ
     */
    public Order update(Order order) {
        log.info("💾 Updating order: id={}, postingNumber={}", order.getId(), order.getPostingNumber());
        
        // Проверяем, изменился ли статус на отмену
        Optional<Order> existingOrder = orderRepository.findById(order.getId());
        if (existingOrder.isPresent() && 
            !existingOrder.get().getStatus().equals(order.getStatus()) && 
            order.getStatus() == OrderStatus.CANCELLED) {
            
            log.info("🔄 Order status changed to CANCELLED: postingNumber={}", order.getPostingNumber());
            // Обновляем остатки через Kafka события при отмене
            stockUpdateService.updateStockForCancelledOrder(order);
        }
        
        Order updatedOrder = orderRepository.save(order);
        log.info("✅ Order updated successfully: id={}, postingNumber={}", 
                updatedOrder.getId(), updatedOrder.getPostingNumber());
        return updatedOrder;
    }

    /**
     * Удалить заказ по ID
     */
    public void deleteById(Long id) {
        log.info("🗑️ Deleting order by ID: {}", id);
        
        if (!orderRepository.existsById(id)) {
            log.warn("❌ Order not found for deletion: id={}", id);
            throw new IllegalArgumentException("Order not found with id: " + id);
        }
        
        orderRepository.deleteById(id);
        log.info("✅ Order deleted successfully: id={}", id);
    }

    /**
     * Проверить существование заказа по ID
     */
    public boolean existsById(Long id) {
        return orderRepository.existsById(id);
    }

    /**
     * Получить заказ по ID
     */
    public Optional<Order> findById(Long id) {
        log.debug("🔍 Searching order by ID: {}", id);
        Optional<Order> order = orderRepository.findById(id);
        
        if (order.isPresent()) {
            log.debug("✅ Order found by ID: {} -> postingNumber={}", id, order.get().getPostingNumber());
        } else {
            log.debug("❌ Order not found by ID: {}", id);
        }
        
        return order;
    }

    // DTO методы
    /**
     * Получить все заказы с пагинацией (DTO)
     */
    public Page<OrderDto> findAllDto(Pageable pageable) {
        log.debug("📋 Fetching orders DTOs with pagination: page={}, size={}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        Page<Order> page = orderRepository.findAll(pageable);
        Page<OrderDto> dtoPage = page.map(orderMapper::toDto);
        log.debug("✅ Orders DTOs page loaded: totalElements={}, totalPages={}", 
                 dtoPage.getTotalElements(), dtoPage.getTotalPages());
        return dtoPage;
    }

    /**
     * Получить заказы по маркету с пагинацией (DTO)
     */
    public Page<OrderDto> findByMarketDto(ru.dmitartur.common.enums.Market market, Pageable pageable) {
        log.debug("📋 Fetching orders DTOs by market: {} with pagination: page={}, size={}", 
                 market, pageable.getPageNumber(), pageable.getPageSize());
        Page<Order> page = orderRepository.findByMarket(market, pageable);
        Page<OrderDto> dtoPage = page.map(orderMapper::toDto);
        log.debug("✅ Orders DTOs by market loaded: market={}, totalElements={}, totalPages={}", 
                 market, dtoPage.getTotalElements(), dtoPage.getTotalPages());
        return dtoPage;
    }

    /**
     * Получить заказы с фильтрами (DTO)
     */
    /**
     * Получить заказы с фильтрами (DTO)
     */
    public Page<OrderDto> findWithFiltersDto(Pageable pageable, String status, String dateFrom, String dateTo) {
        log.debug("📋 Fetching orders DTOs with filters: status={}, dateFrom={}, dateTo={}, page={}, size={}", 
                 status, dateFrom, dateTo, pageable.getPageNumber(), pageable.getPageSize());
        
        ZonedDateTime fromDate = null;
        ZonedDateTime toDate = null;
        
        try {
            if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                fromDate = ZonedDateTime.parse(dateFrom);
            }
            if (dateTo != null && !dateTo.trim().isEmpty()) {
                toDate = ZonedDateTime.parse(dateTo);
            }
        } catch (Exception e) {
            log.warn("❌ Invalid date format: dateFrom={}, dateTo={}", dateFrom, dateTo);
        }
        
        Page<Order> page = orderRepository.findByFilters(status, fromDate, toDate, pageable);
        Page<OrderDto> dtoPage = page.map(orderMapper::toDto);
        
        log.debug("✅ Orders DTOs with filters loaded: totalElements={}, totalPages={}", 
                 dtoPage.getTotalElements(), dtoPage.getTotalPages());
        return dtoPage;
    }

    /**
     * Получить заказы по маркету с фильтрами (DTO)
     */
    public Page<OrderDto> findByMarketWithFiltersDto(ru.dmitartur.common.enums.Market market, Pageable pageable, 
                                                    String status, String dateFrom, String dateTo) {
        log.debug("📋 Fetching orders DTOs by market with filters: market={}, status={}, dateFrom={}, dateTo={}, page={}, size={}", 
                 market, status, dateFrom, dateTo, pageable.getPageNumber(), pageable.getPageSize());
        
        ZonedDateTime fromDate = null;
        ZonedDateTime toDate = null;
        
        try {
            if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                fromDate = ZonedDateTime.parse(dateFrom);
            }
            if (dateTo != null && !dateTo.trim().isEmpty()) {
                toDate = ZonedDateTime.parse(dateTo);
            }
        } catch (Exception e) {
            log.warn("❌ Invalid date format: dateFrom={}, dateTo={}", dateFrom, dateTo);
        }
        
        Page<Order> page = orderRepository.findByMarketAndFilters(market, status, fromDate, toDate, pageable);
        Page<OrderDto> dtoPage = page.map(orderMapper::toDto);
        log.debug("✅ Orders DTOs by market with filters loaded: market={}, totalElements={}, totalPages={}", 
                 market, dtoPage.getTotalElements(), dtoPage.getTotalPages());
        return dtoPage;
    }

    /**
     * Найти заказ по номеру постинга (DTO)
     */
    public Optional<OrderDto> findByPostingNumberDto(String postingNumber) {
        log.debug("🔍 Searching order DTO by posting number: {}", postingNumber);
        Optional<Order> order = orderRepository.findByPostingNumber(postingNumber);
        
        if (order.isPresent()) {
            log.debug("✅ Order DTO found by posting number: {} -> id={}", postingNumber, order.get().getId());
            return Optional.of(orderMapper.toDto(order.get()));
        } else {
            log.debug("❌ Order DTO not found by posting number: {}", postingNumber);
            return Optional.empty();
        }
    }

    /**
     * Сохранить заказ (DTO)
     */
    public OrderDto saveDto(OrderDto orderDto) {
        log.info("💾 Saving order DTO: postingNumber={}", orderDto.getPostingNumber());
        Order entity = orderMapper.toEntity(orderDto);
        Order savedOrder = orderRepository.save(entity);
        OrderDto savedDto = orderMapper.toDto(savedOrder);
        log.info("✅ Order DTO saved successfully: id={}, postingNumber={}", 
                savedDto.getId(), savedDto.getPostingNumber());
        return savedDto;
    }

    /**
     * Обновить заказ (DTO)
     */
    public OrderDto updateDto(OrderDto orderDto) {
        log.info("💾 Updating order DTO: id={}, postingNumber={}", orderDto.getId(), orderDto.getPostingNumber());
        Order entity = orderMapper.toEntity(orderDto);
        Order updatedOrder = orderRepository.save(entity);
        OrderDto updatedDto = orderMapper.toDto(updatedOrder);
        log.info("✅ Order DTO updated successfully: id={}, postingNumber={}", 
                updatedDto.getId(), updatedDto.getPostingNumber());
        return updatedDto;
    }

    /**
     * Получить заказ по ID (DTO)
     */
    public Optional<OrderDto> findByIdDto(Long id) {
        log.debug("🔍 Searching order DTO by ID: {}", id);
        Optional<Order> order = orderRepository.findById(id);
        
        if (order.isPresent()) {
            log.debug("✅ Order DTO found by ID: {} -> postingNumber={}", id, order.get().getPostingNumber());
            return Optional.of(orderMapper.toDto(order.get()));
        } else {
            log.debug("❌ Order DTO not found by ID: {}", id);
            return Optional.empty();
        }
    }

    /**
     * Пакетное обновление заказов из Ozon
     */
    @Transactional
    public int upsertBatch(JsonNode payload) {
        return orderBatchProcessor.processOzonBatch(payload);
    }
    

}


