package org.example.mapper;

import org.example.dto.ProductMarketDto;
import org.example.entity.ProductMarket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {MarketMapper.class})
public interface ProductMarketMapper {
    ProductMarketMapper INSTANCE = Mappers.getMapper(ProductMarketMapper.class);

    @Mapping(target = "product", ignore = true)
    ProductMarketDto toDto(ProductMarket entity);

    ProductMarket toEntity(ProductMarketDto dto);
} 