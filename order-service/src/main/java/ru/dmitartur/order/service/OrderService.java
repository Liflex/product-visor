package ru.dmitartur.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.entity.OrderItem;
import ru.dmitartur.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public int upsertBatch(JsonNode payload) {
        int count = 0;
        if (payload == null || !payload.has("postings")) return 0;
        for (JsonNode posting : payload.get("postings")) {
            String postingNumber = posting.path("postingNumber").asText(posting.path("posting_number").asText());
            if (postingNumber == null || postingNumber.isEmpty()) continue;
            Order order = orderRepository.findByPostingNumber(postingNumber).orElseGet(Order::new);
            order.setPostingNumber(postingNumber);
            order.setSource("OZON_FBO");
            order.setStatus(posting.path("status").asText("unknown"));
            String created = posting.path("createdAt").asText(posting.path("created_at").asText(null));
            if (created != null) order.setCreatedAt(OffsetDateTime.parse(created));
            order.setUpdatedAt(OffsetDateTime.now());

            order.getItems().clear();
            if (posting.has("products")) {
                for (JsonNode item : posting.get("products")) {
                    OrderItem oi = new OrderItem();
                    oi.setOrder(order);
                    oi.setProductId(item.path("productId").asLong(item.path("product_id").asLong()));
                    oi.setOfferId(item.path("offerId").asText(item.path("offer_id").asText(null)));
                    oi.setName(item.path("name").asText(null));
                    oi.setQuantity(item.path("quantity").asInt(1));
                    if (item.has("price")) { try { oi.setPrice(new BigDecimal(item.get("price").asText())); } catch (Exception ignored) {} }
                    order.getItems().add(oi);
                }
            }
            orderRepository.save(order);
            count++;
        }
        return count;
    }
}


