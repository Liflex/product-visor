package org.example.mapper;

import org.example.dto.MarketDto;
import org.example.entity.Market;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MarketMapper {
    MarketMapper INSTANCE = Mappers.getMapper(MarketMapper.class);
    MarketDto toDto(Market market);
    Market toEntity(MarketDto dto);
} 