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
    date = "2025-08-02T16:34:32+0300",
    comments = "version: 1.6.2, compiler: Eclipse JDT (IDE) 3.42.50.v20250628-1110, environment: Java 21.0.7 (Azul Systems, Inc.)"
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
        productMarketDto.setMarket( marketMapper.toDto( entity.getMarket() ) );
        productMarketDto.setPrice( entity.getPrice() );
        productMarketDto.setQuantity( entity.getQuantity() );

        return productMarketDto;
    }

    @Override
    public ProductMarket toEntity(ProductMarketDto dto) {
        if ( dto == null ) {
            return null;
        }

        ProductMarket productMarket = new ProductMarket();

        productMarket.setId( dto.getId() );
        productMarket.setMarket( marketMapper.toEntity( dto.getMarket() ) );
        productMarket.setPrice( dto.getPrice() );
        productMarket.setProduct( productDtoToProduct( dto.getProduct() ) );
        productMarket.setQuantity( dto.getQuantity() );

        return productMarket;
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

        category.setAttributes( attributeDtoListToAttributeList( categoryDto.getAttributes() ) );
        category.setId( categoryDto.getId() );
        category.setName( categoryDto.getName() );

        return category;
    }

    protected ProductAttributeValue productAttributeValueDtoToProductAttributeValue(ProductAttributeValueDto productAttributeValueDto) {
        if ( productAttributeValueDto == null ) {
            return null;
        }

        ProductAttributeValue productAttributeValue = new ProductAttributeValue();

        productAttributeValue.setAttribute( attributeDtoToAttribute( productAttributeValueDto.getAttribute() ) );
        productAttributeValue.setId( productAttributeValueDto.getId() );
        productAttributeValue.setValue( productAttributeValueDto.getValue() );

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

        product.setArticle( productDto.getArticle() );
        product.setBarcode( productDto.getBarcode() );
        product.setCategory( categoryDtoToCategory( productDto.getCategory() ) );
        product.setId( productDto.getId() );
        byte[] image = productDto.getImage();
        if ( image != null ) {
            product.setImage( Arrays.copyOf( image, image.length ) );
        }
        product.setImageUrl( productDto.getImageUrl() );
        product.setName( productDto.getName() );
        product.setPackageInfo( productDto.getPackageInfo() );
        product.setPrice( productDto.getPrice() );
        product.setProductAttributeValues( productAttributeValueDtoListToProductAttributeValueList( productDto.getProductAttributeValues() ) );
        product.setProductMarkets( productMarketDtoListToProductMarketList( productDto.getProductMarkets() ) );
        product.setQuantity( productDto.getQuantity() );

        return product;
    }
}
