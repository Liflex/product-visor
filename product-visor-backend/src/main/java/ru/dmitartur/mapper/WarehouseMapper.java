package ru.dmitartur.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.dmitartur.dto.WarehouseDto;
import ru.dmitartur.entity.Warehouse;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    WarehouseMapper INSTANCE = Mappers.getMapper(WarehouseMapper.class);

    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "isHomeWarehouse", source = "homeWarehouse")
    WarehouseDto toDto(Warehouse entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "isHomeWarehouse", source = "isHomeWarehouse")
    Warehouse toEntity(WarehouseDto dto);
}


