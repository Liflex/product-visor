package ru.dmitartur.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.dmitartur.order.dto.OrderDto;
import ru.dmitartur.order.dto.OrderItemDto;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.entity.OrderItem;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemDtoMapper.class})
public interface OrderMapper {

    // Методы для создания Order из примитивов (бывший OrderMapper)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "postingNumber", source = "postingNumber")
    @Mapping(target = "source", constant = "OZON_FBO")
    @Mapping(target = "market", constant = "OZON")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", expression = "java(now())")
    @Mapping(target = "ozonCreatedAt", source = "createdAt")
    @Mapping(target = "updatedAt", expression = "java(now())")
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "customerPhone", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "items", ignore = true)
    Order toEntity(String postingNumber, String status, OffsetDateTime createdAt);

    // Методы для Entity ↔ DTO преобразования (бывший OrderDtoMapper)
    @Mapping(target = "items", source = "items")
    OrderDto toDto(Order order);
    
    @Mapping(target = "items", source = "items")
    Order toEntity(OrderDto orderDto);
    
    List<OrderDto> toDtoList(List<Order> orders);
    
    List<Order> toEntityList(List<OrderDto> orderDtos);

    default OffsetDateTime now() { return OffsetDateTime.now(); }
}





