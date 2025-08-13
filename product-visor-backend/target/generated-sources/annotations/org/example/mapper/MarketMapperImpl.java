package org.example.mapper;

import java.util.Arrays;
import javax.annotation.processing.Generated;
import org.example.dto.MarketDto;
import org.example.entity.Market;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-13T20:56:49+0300",
    comments = "version: 1.6.2, compiler: javac, environment: Java 17.0.14 (Azul Systems, Inc.)"
)
@Component
public class MarketMapperImpl implements MarketMapper {

    @Override
    public MarketDto toDto(Market market) {
        if ( market == null ) {
            return null;
        }

        MarketDto marketDto = new MarketDto();

        marketDto.setId( market.getId() );
        marketDto.setName( market.getName() );
        marketDto.setDescription( market.getDescription() );
        marketDto.setImageUrl( market.getImageUrl() );
        byte[] image = market.getImage();
        if ( image != null ) {
            marketDto.setImage( Arrays.copyOf( image, image.length ) );
        }

        return marketDto;
    }

    @Override
    public Market toEntity(MarketDto dto) {
        if ( dto == null ) {
            return null;
        }

        Market market = new Market();

        market.setId( dto.getId() );
        market.setName( dto.getName() );
        market.setDescription( dto.getDescription() );
        market.setImageUrl( dto.getImageUrl() );
        byte[] image = dto.getImage();
        if ( image != null ) {
            market.setImage( Arrays.copyOf( image, image.length ) );
        }

        return market;
    }
}
