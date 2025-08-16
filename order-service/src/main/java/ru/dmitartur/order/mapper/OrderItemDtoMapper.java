package ru.dmitartur.order.mapper;

import org.mapstruct.Mapper;
import ru.dmitartur.order.dto.OrderItemDto;
import ru.dmitartur.order.entity.OrderItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemDtoMapper {
    
    OrderItemDto toDto(OrderItem orderItem);
    
    OrderItem toEntity(OrderItemDto orderItemDto);
    
    List<OrderItemDto> toDtoList(List<OrderItem> orderItems);
    
    List<OrderItem> toEntityList(List<OrderItemDto> orderItemDtos);
}
