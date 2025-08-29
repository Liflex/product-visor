package ru.dmitartur.mapper;

import ru.dmitartur.dto.ProductStockDto;
import ru.dmitartur.dto.WarehouseDto;
import ru.dmitartur.entity.ProductStock;
import ru.dmitartur.entity.Warehouse;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {WarehouseMapper.class})
public interface ProductStockMapper {
    ProductStockMapper INSTANCE = Mappers.getMapper(ProductStockMapper.class);

    @Mapping(target = "warehouses", source = "warehouses")
    ProductStockDto toDto(ProductStock productStock);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "warehouses", source = "warehouses")
    ProductStock toEntity(ProductStockDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "warehouses", source = "warehouses")
    void updateEntityFromDto(ProductStockDto dto, @MappingTarget ProductStock productStock);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "warehouses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateProductStock(@MappingTarget ProductStock oldProductStock, ProductStock newProductStock);
}
