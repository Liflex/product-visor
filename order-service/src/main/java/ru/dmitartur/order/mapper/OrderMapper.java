package ru.dmitartur.order.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Named;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.entity.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring",
        uses = { OrderItemDtoMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    // removed simple factory method to avoid MapStruct generation issues

    Order toEntity(OrderDto orderDto);

    @AfterMapping
    default void setBackReferences(@MappingTarget Order order) {
        List<OrderItem> items = order.getItems();
        if (items != null) {
            for (OrderItem item : items) {
                if (item != null) {
                    item.setOrder(order);
                }
            }
        }
    }

    OrderDto toDto(Order order);

    // Custom update: respectful of persistence context - update existing items, add new, remove missing
    @SuppressWarnings({"DuplicatedCode", "java:S3776"})
    default void updateOrder(@MappingTarget Order oldOrder, Order newOrder) {
        if (newOrder == null) return;

        // 1) Scalar fields
        oldOrder.setPostingNumber(newOrder.getPostingNumber());
        oldOrder.setSource(newOrder.getSource());
        oldOrder.setMarket(newOrder.getMarket());
        oldOrder.setStatus(newOrder.getStatus());
        oldOrder.setCreatedAt(newOrder.getCreatedAt());
        oldOrder.setUpdatedAt(newOrder.getUpdatedAt());
        oldOrder.setOzonCreatedAt(newOrder.getOzonCreatedAt());
        oldOrder.setCustomerName(newOrder.getCustomerName());
        oldOrder.setCustomerPhone(newOrder.getCustomerPhone());
        oldOrder.setAddress(newOrder.getAddress());
        oldOrder.setTotalPrice(newOrder.getTotalPrice());
        oldOrder.setInProcessAt(newOrder.getInProcessAt());
        oldOrder.setShipmentDate(newOrder.getShipmentDate());
        oldOrder.setDeliveringDate(newOrder.getDeliveringDate());
        oldOrder.setCancelReason(newOrder.getCancelReason());
        oldOrder.setCancelReasonId(newOrder.getCancelReasonId());
        oldOrder.setCancellationType(newOrder.getCancellationType());
        oldOrder.setTrackingNumber(newOrder.getTrackingNumber());
        oldOrder.setDeliveryMethodName(newOrder.getDeliveryMethodName());
        oldOrder.setSubstatus(newOrder.getSubstatus());
        oldOrder.setIsExpress(newOrder.getIsExpress());
        oldOrder.setWarehouseId(newOrder.getWarehouseId());

        // 2) Items diff: by id first, then by business key (offerId + sku)
        List<OrderItem> currentItems = oldOrder.getItems();
        List<OrderItem> incoming = newOrder.getItems();

        if (incoming == null || incoming.isEmpty()) {
            // Remove all existing items if incoming is empty
            currentItems.clear();
            return;
        }

        // Build lookup maps for existing items
        java.util.Map<Long, OrderItem> byId = new java.util.HashMap<>();
        java.util.Map<String, OrderItem> byBusinessKey = new java.util.HashMap<>();
        for (OrderItem existing : currentItems) {
            if (existing.getId() != null) byId.put(existing.getId(), existing);
            byBusinessKey.put(makeBusinessKey(existing.getOfferId(), existing.getSku()), existing);
        }

        // Track which existing ones are matched
        java.util.Set<OrderItem> matched = new java.util.HashSet<>();

        // Upsert incoming
        for (OrderItem newItem : incoming) {
            if (newItem == null) continue;

            OrderItem target = null;
            if (newItem.getId() != null) {
                target = byId.get(newItem.getId());
            }
            if (target == null) {
                target = byBusinessKey.get(makeBusinessKey(newItem.getOfferId(), newItem.getSku()));
            }

            if (target != null) {
                // Update existing item in-place
                copyOrderItemFields(target, newItem);
                target.setOrder(oldOrder);
                matched.add(target);
            } else {
                // New item - attach and add
                newItem.setOrder(oldOrder);
                currentItems.add(newItem);
                matched.add(newItem);
            }
        }

        // Remove items not matched (orphanRemoval will delete)
        currentItems.removeIf(item -> !matched.contains(item));
    }

    default String makeBusinessKey(String offerId, String sku) {
        return (offerId == null ? "" : offerId) + "|" + (sku == null ? "" : sku);
    }

    default void copyOrderItemFields(OrderItem target, OrderItem src) {
        target.setProductId(src.getProductId());
        target.setSku(src.getSku());
        target.setOfferId(src.getOfferId());
        target.setName(src.getName());
        target.setQuantity(src.getQuantity());
        target.setPrice(src.getPrice());
    }

    @Named("now")
    default LocalDateTime now() { return LocalDateTime.now(); }
}






