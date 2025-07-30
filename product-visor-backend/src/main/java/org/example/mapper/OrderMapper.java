package org.example.mapper;

import org.example.dto.OrderDto;
import org.example.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, MarketMapper.class})
public interface OrderMapper {
    
    @Mapping(target = "product", source = "product")
    @Mapping(target = "market", source = "market")
    OrderDto toDto(Order order);
    
    @Mapping(target = "product", source = "product")
    @Mapping(target = "market", source = "market")
    @Mapping(target = "orderDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", constant = "PURCHASED")
    Order toEntity(OrderDto orderDto);
} 