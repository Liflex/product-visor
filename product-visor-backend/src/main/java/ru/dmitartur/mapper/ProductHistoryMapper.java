package ru.dmitartur.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.dmitartur.dto.ProductHistoryDto;
import ru.dmitartur.entity.ProductHistory;

/**
 * Mapper для преобразования ProductHistory в DTO
 */
@Mapper(componentModel = "spring")
public interface ProductHistoryMapper {
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "fieldName", source = "fieldName")
    @Mapping(target = "oldValue", source = "oldValue")
    @Mapping(target = "newValue", source = "newValue")
    @Mapping(target = "changeReason", source = "changeReason")
    @Mapping(target = "sourceSystem", source = "sourceSystem")
    @Mapping(target = "sourceId", source = "sourceId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "metadata", source = "metadata")
    ProductHistoryDto toDto(ProductHistory entity);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "fieldName", source = "fieldName")
    @Mapping(target = "oldValue", source = "oldValue")
    @Mapping(target = "newValue", source = "newValue")
    @Mapping(target = "changeReason", source = "changeReason")
    @Mapping(target = "sourceSystem", source = "sourceSystem")
    @Mapping(target = "sourceId", source = "sourceId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "metadata", source = "metadata")
    ProductHistory toEntity(ProductHistoryDto dto);
}
