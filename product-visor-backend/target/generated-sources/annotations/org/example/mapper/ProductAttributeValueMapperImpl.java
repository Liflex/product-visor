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
    date = "2025-08-13T20:56:49+0300",
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
        productAttributeValueDto.setAttribute( attributeToAttributeDto( attribute.getAttribute() ) );

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
        productAttributeValue.setAttribute( attributeDtoToAttribute( attributeDto.getAttribute() ) );

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
        attributeDto.setName( attribute.getName() );
        attributeDto.setNameRus( attribute.getNameRus() );
        attributeDto.setType( attribute.getType() );
        attributeDto.setRequired( attribute.isRequired() );
        attributeDto.setMultiple( attribute.isMultiple() );

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
        attribute.setName( attributeDto.getName() );
        attribute.setNameRus( attributeDto.getNameRus() );
        attribute.setType( attributeDto.getType() );
        attribute.setRequired( attributeDto.isRequired() );
        attribute.setMultiple( attributeDto.isMultiple() );

        return attribute;
    }
}
