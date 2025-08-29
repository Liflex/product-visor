package ru.dmitartur.order.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.event.OrderCancelledEvent;
import ru.dmitartur.order.event.OrderCreatedEvent;
import ru.dmitartur.order.mapper.OrderMapper;
import ru.dmitartur.order.repository.OrderRepository;
import ru.dmitartur.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    // Helpers for controller-level conversions
    @Getter
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

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

        // Публикуем событие создания заказа
        eventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder));

        return savedOrder;
    }

    /**
     * Обновить заказ
     */
    public Order update(Order newOrder) {
        log.info("💾 Updating order: id={}, postingNumber={}", newOrder.getId(), newOrder.getPostingNumber());

        // Проверяем, изменился ли статус на отмену
        Optional<Order> existingOrder = orderRepository.findByPostingNumber(newOrder.getPostingNumber());
        if (existingOrder.isPresent() &&
                !existingOrder.get().getStatus().equals(newOrder.getStatus()) &&
                newOrder.getStatus() == OrderStatus.CANCELLED) {
            log.info("🔄 Order status changed to CANCELLED: postingNumber={}", newOrder.getPostingNumber());
            // Публикуем событие отмены заказа
            eventPublisher.publishEvent(new OrderCancelledEvent(this, newOrder));
        }

        Order oldOrder = existingOrder.get();
        orderMapper.updateOrder(oldOrder, newOrder);
        Order updatedOrder = orderRepository.save(oldOrder);
        log.info("✅ Order updated successfully: id={}, postingNumber={}",
                updatedOrder.getId(), updatedOrder.getPostingNumber());
        return updatedOrder;
    }

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

        var ownerIdOpt = ru.dmitartur.common.utils.JwtUtil.getCurrentId();
        if (ownerIdOpt.isEmpty()) return Page.empty(pageable);
        UUID ownerId = ru.dmitartur.common.utils.JwtUtil.getRequiredOwnerId();
        Page<Order> page = orderRepository.findByFiltersAndOwner(ownerId, status, fromDate, toDate, pageable);
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

        var ownerIdOpt = ru.dmitartur.common.utils.JwtUtil.getCurrentId();
        if (ownerIdOpt.isEmpty()) return Page.empty(pageable);
        UUID ownerId = ru.dmitartur.common.utils.JwtUtil.getRequiredOwnerId();
        Page<Order> page = orderRepository.findByMarketAndFiltersAndOwner(market, ownerId, status, fromDate, toDate, pageable);
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
        var ownerIdOpt = ru.dmitartur.common.utils.JwtUtil.getCurrentId();
        if (ownerIdOpt.isEmpty()) return Optional.empty();
        UUID ownerId = ru.dmitartur.common.utils.JwtUtil.getRequiredOwnerId();
        Optional<Order> order = orderRepository.findByPostingNumberAndOwnerUserId(postingNumber, ownerId);

        if (order.isPresent()) {
            log.debug("✅ Order DTO found by posting number: {} -> id={}", postingNumber, order.get().getId());
            return Optional.of(orderMapper.toDto(order.get()));
        } else {
            log.debug("❌ Order DTO not found by posting number: {}", postingNumber);
            return Optional.empty();
        }
    }

}


