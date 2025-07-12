package org.example.mapper;

import org.example.dto.ProductDto;
import org.example.entity.Product;
import org.example.entity.ProductMarket;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, ProductAttributeValueMapper.class, MarketMapper.class, ProductMarketMapper.class})
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    ProductDto toDto(Product product);

    @BeanMapping(qualifiedByName = "enrichPickupProductMarketReverseUrls")
    Product toEntity(ProductDto dto);

    @AfterMapping
    @Named("enrichPickupProductMarketReverseUrls")
    default void enrichPickupProductMarketReverseUrls(@MappingTarget Product product) {
        if(product != null && product.getProductMarkets() != null) {
            for (ProductMarket productMarket : product.getProductMarkets()) {
                productMarket.setProduct(product);
            }
        }
    };
}
