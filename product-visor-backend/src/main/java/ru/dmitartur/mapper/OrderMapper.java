package ru.dmitartur.mapper;

import ru.dmitartur.dto.OrderDto;
// import ru.dmitartur.entity.Order; // Order entity removed from backend; mapping disabled
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface OrderMapper {
    // OrderDto toDto(Order order);
} 