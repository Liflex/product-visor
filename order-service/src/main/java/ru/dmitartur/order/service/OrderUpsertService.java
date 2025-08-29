package ru.dmitartur.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.mapper.OrderMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderUpsertService {

    private final OrderMapper orderMapper;
    private final OrderService orderService;

    @Transactional
    public void upsert(OrderDto orderDto) {
        try {
            var existingOrder = orderService.findByPostingNumber(orderDto.getPostingNumber());
            Order entity = orderMapper.toEntity(orderDto);

            if (existingOrder.isPresent()) {
                orderService.update(entity);
                log.debug("🔄 Updated existing order: {}", orderDto.getPostingNumber());
            } else {
                orderService.save(entity);
                log.debug("✅ Created new order: {}", orderDto.getPostingNumber());
            }

        } catch (Exception e) {
            log.error("❌ Error upserting order {}: {}", orderDto.getPostingNumber(), e.getMessage());
            throw e;
        }
    }
}


