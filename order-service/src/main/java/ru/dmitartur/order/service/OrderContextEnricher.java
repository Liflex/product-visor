package ru.dmitartur.order.service;

import org.springframework.stereotype.Component;
import ru.dmitartur.common.utils.JwtUtil;
import ru.dmitartur.order.entity.Order;

import java.util.UUID;

@Component
public class OrderContextEnricher {
    public void enrich(Order order) {
        JwtUtil.getCurrentId().ifPresent(id -> {
            try { order.setOwnerUserId(UUID.fromString(id)); } catch (Exception ignored) {}
        });
        JwtUtil.resolveEffectiveCompanyId().ifPresent(cid -> {
            try { order.setCompanyId(UUID.fromString(cid)); } catch (Exception ignored) {}
        });
    }
}




