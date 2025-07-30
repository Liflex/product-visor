package org.example.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.Generated;
import org.example.dto.AttributeDto;
import org.example.dto.CategoryDto;
import org.example.dto.ProductAttributeValueDto;
import org.example.dto.ProductDto;
import org.example.dto.ProductMarketDto;
import org.example.entity.Attribute;
import org.example.entity.Category;
import org.example.entity.Product;
import org.example.entity.ProductAttributeValue;
import org.example.entity.ProductMarket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-12T22:47:33+0300",
    comments = "version: 1.6.2, compiler: javac, environment: Java 17.0.14 (Azul Systems, Inc.)"
)
@Component
public class ProductMarketMapperImpl implements ProductMarketMapper {

    @Autowired
    private MarketMapper marketMapper;

    @Override
    public ProductMarketDto toDto(ProductMarket entity) {
        if ( entity == null ) {
            return null;
        }

        ProductMarketDto productMarketDto = new ProductMarketDto();

        productMarketDto.setId( entity.getId() );
        productMarketDto.setQuantity( entity.getQuantity() );
        productMarketDto.setPrice( entity.getPrice() );
        productMarketDto.setMarket( marketMapper.toDto( entity.getMarket() ) );

        return productMarketDto;
    }

    @Override
    public ProductMarket toEntity(ProductMarketDto dto) {
        if ( dto == null ) {
            return null;
        }

        ProductMarket productMarket = new ProductMarket();

        productMarket.setId( dto.getId() );
        productMarket.setProduct( productDtoToProduct( dto.getProduct() ) );
        productMarket.setMarket( marketMapper.toEntity( dto.getMarket() ) );
        productMarket.setQuantity( dto.getQuantity() );
        productMarket.setPrice( dto.getPrice() );

        return productMarket;
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

    protected List<Attribute> attributeDtoListToAttributeList(List<AttributeDto> list) {
        if ( list == null ) {
            return null;
        }

        List<Attribute> list1 = new ArrayList<Attribute>( list.size() );
        for ( AttributeDto attributeDto : list ) {
            list1.add( attributeDtoToAttribute( attributeDto ) );
        }

        return list1;
    }

    protected Category categoryDtoToCategory(CategoryDto categoryDto) {
        if ( categoryDto == null ) {
            return null;
        }

        Category category = new Category();

        category.setId( categoryDto.getId() );
        category.setName( categoryDto.getName() );
        category.setAttributes( attributeDtoListToAttributeList( categoryDto.getAttributes() ) );

        return category;
    }

    protected ProductAttributeValue productAttributeValueDtoToProductAttributeValue(ProductAttributeValueDto productAttributeValueDto) {
        if ( productAttributeValueDto == null ) {
            return null;
        }

        ProductAttributeValue productAttributeValue = new ProductAttributeValue();

        productAttributeValue.setId( productAttributeValueDto.getId() );
        productAttributeValue.setValue( productAttributeValueDto.getValue() );
        productAttributeValue.setAttribute( attributeDtoToAttribute( productAttributeValueDto.getAttribute() ) );

        return productAttributeValue;
    }

    protected List<ProductAttributeValue> productAttributeValueDtoListToProductAttributeValueList(List<ProductAttributeValueDto> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductAttributeValue> list1 = new ArrayList<ProductAttributeValue>( list.size() );
        for ( ProductAttributeValueDto productAttributeValueDto : list ) {
            list1.add( productAttributeValueDtoToProductAttributeValue( productAttributeValueDto ) );
        }

        return list1;
    }

    protected List<ProductMarket> productMarketDtoListToProductMarketList(List<ProductMarketDto> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductMarket> list1 = new ArrayList<ProductMarket>( list.size() );
        for ( ProductMarketDto productMarketDto : list ) {
            list1.add( toEntity( productMarketDto ) );
        }

        return list1;
    }

    protected Product productDtoToProduct(ProductDto productDto) {
        if ( productDto == null ) {
            return null;
        }

        Product product = new Product();

        product.setId( productDto.getId() );
        product.setName( productDto.getName() );
        byte[] image = productDto.getImage();
        if ( image != null ) {
            product.setImage( Arrays.copyOf( image, image.length ) );
        }
        product.setImageUrl( productDto.getImageUrl() );
        product.setBarcode( productDto.getBarcode() );
        product.setQuantity( productDto.getQuantity() );
        product.setCategory( categoryDtoToCategory( productDto.getCategory() ) );
        product.setProductAttributeValues( productAttributeValueDtoListToProductAttributeValueList( productDto.getProductAttributeValues() ) );
        product.setProductMarkets( productMarketDtoListToProductMarketList( productDto.getProductMarkets() ) );

        return product;
    }
}
