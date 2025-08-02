package org.example.mapper;

import java.util.Arrays;
import javax.annotation.processing.Generated;
import org.example.dto.MarketDto;
import org.example.entity.Market;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-02T16:34:32+0300",
    comments = "version: 1.6.2, compiler: Eclipse JDT (IDE) 3.42.50.v20250628-1110, environment: Java 21.0.7 (Azul Systems, Inc.)"
)
@Component
public class MarketMapperImpl implements MarketMapper {

    @Override
    public MarketDto toDto(Market market) {
        if ( market == null ) {
            return null;
        }

        MarketDto marketDto = new MarketDto();

        marketDto.setDescription( market.getDescription() );
        marketDto.setId( market.getId() );
        byte[] image = market.getImage();
        if ( image != null ) {
            marketDto.setImage( Arrays.copyOf( image, image.length ) );
        }
        marketDto.setImageUrl( market.getImageUrl() );
        marketDto.setName( market.getName() );

        return marketDto;
    }

    @Override
    public Market toEntity(MarketDto dto) {
        if ( dto == null ) {
            return null;
        }

        Market market = new Market();

        market.setDescription( dto.getDescription() );
        market.setId( dto.getId() );
        byte[] image = dto.getImage();
        if ( image != null ) {
            market.setImage( Arrays.copyOf( image, image.length ) );
        }
        market.setImageUrl( dto.getImageUrl() );
        market.setName( dto.getName() );

        return market;
    }
}
