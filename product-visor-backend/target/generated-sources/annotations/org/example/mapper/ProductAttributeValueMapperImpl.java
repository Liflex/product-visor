package org.example.mapper;

import javax.annotation.processing.Generated;
import org.example.dto.AttributeDto;
import org.example.dto.ProductAttributeValueDto;
import org.example.entity.Attribute;
import org.example.entity.Product;
import org.example.entity.ProductAttributeValue;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-02T16:34:32+0300",
    comments = "version: 1.6.2, compiler: Eclipse JDT (IDE) 3.42.50.v20250628-1110, environment: Java 21.0.7 (Azul Systems, Inc.)"
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
        productAttributeValueDto.setAttribute( attributeToAttributeDto( attribute.getAttribute() ) );
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
        productAttributeValue.setAttribute( attributeDtoToAttribute( attributeDto.getAttribute() ) );
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

    protected AttributeDto attributeToAttributeDto(Attribute attribute) {
        if ( attribute == null ) {
            return null;
        }

        AttributeDto attributeDto = new AttributeDto();

        attributeDto.setId( attribute.getId() );
        attributeDto.setMultiple( attribute.isMultiple() );
        attributeDto.setName( attribute.getName() );
        attributeDto.setNameRus( attribute.getNameRus() );
        attributeDto.setRequired( attribute.isRequired() );
        attributeDto.setType( attribute.getType() );

        return attributeDto;
    }

    protected Product productAttributeValueDtoToProduct(ProductAttributeValueDto productAttributeValueDto) {
        if ( productAttributeValueDto == null ) {
            return null;
        }

        Product product = new Product();

        product.setId( productAttributeValueDto.getProductId() );

        return product;
    }

    protected Attribute attributeDtoToAttribute(AttributeDto attributeDto) {
        if ( attributeDto == null ) {
            return null;
        }

        Attribute attribute = new Attribute();

        attribute.setId( attributeDto.getId() );
        attribute.setMultiple( attributeDto.isMultiple() );
        attribute.setName( attributeDto.getName() );
        attribute.setNameRus( attributeDto.getNameRus() );
        attribute.setRequired( attributeDto.isRequired() );
        attribute.setType( attributeDto.getType() );

        return attribute;
    }
}
