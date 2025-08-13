package ru.dmitartur.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.dmitartur.dto.ProductAttributeValueDto;
import ru.dmitartur.entity.Product;
import ru.dmitartur.entity.ProductAttributeValue;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-14T00:54:23+0300",
    comments = "version: 1.6.2, compiler: javac, environment: Java 17.0.14 (Azul Systems, Inc.)"
)
@Component
public class ProductAttributeValueMapperImpl implements ProductAttributeValueMapper {

    @Override
    public ProductAttributeValueDto toDto(ProductAttributeValue attribute) {
        if ( attribute == null ) {
            return null;
        }

        ProductAttributeValueDto productAttributeValueDto = new ProductAttributeValueDto();

        productAttributeValueDto.setProductId( attributeProductId( attribute ) );
        productAttributeValueDto.setId( attribute.getId() );
        productAttributeValueDto.setValue( attribute.getValue() );

        return productAttributeValueDto;
    }

    @Override
    public ProductAttributeValue toEntity(ProductAttributeValueDto attributeDto) {
        if ( attributeDto == null ) {
            return null;
        }

        ProductAttributeValue productAttributeValue = new ProductAttributeValue();

        productAttributeValue.setProduct( productAttributeValueDtoToProduct( attributeDto ) );
        productAttributeValue.setId( attributeDto.getId() );
        productAttributeValue.setValue( attributeDto.getValue() );

        return productAttributeValue;
    }

    private Long attributeProductId(ProductAttributeValue productAttributeValue) {
        Product product = productAttributeValue.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getId();
    }

    protected Product productAttributeValueDtoToProduct(ProductAttributeValueDto productAttributeValueDto) {
        if ( productAttributeValueDto == null ) {
            return null;
        }

        Product product = new Product();

        product.setId( productAttributeValueDto.getProductId() );

        return product;
    }
}
