package org.example.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.Generated;
import org.example.dto.ProductAttributeValueDto;
import org.example.dto.ProductDto;
import org.example.dto.ProductMarketDto;
import org.example.entity.Product;
import org.example.entity.ProductAttributeValue;
import org.example.entity.ProductMarket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-02T16:34:33+0300",
    comments = "version: 1.6.2, compiler: Eclipse JDT (IDE) 3.42.50.v20250628-1110, environment: Java 21.0.7 (Azul Systems, Inc.)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ProductAttributeValueMapper productAttributeValueMapper;
    @Autowired
    private ProductMarketMapper productMarketMapper;

    @Override
    public ProductDto toDto(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductDto productDto = new ProductDto();

        productDto.setArticle( product.getArticle() );
        productDto.setBarcode( product.getBarcode() );
        productDto.setCategory( categoryMapper.categoryToCategoryDto( product.getCategory() ) );
        productDto.setId( product.getId() );
        byte[] image = product.getImage();
        if ( image != null ) {
            productDto.setImage( Arrays.copyOf( image, image.length ) );
        }
        productDto.setImageUrl( product.getImageUrl() );
        productDto.setName( product.getName() );
        productDto.setPackageInfo( product.getPackageInfo() );
        productDto.setPrice( product.getPrice() );
        productDto.setProductAttributeValues( productAttributeValueListToProductAttributeValueDtoList( product.getProductAttributeValues() ) );
        productDto.setProductMarkets( productMarketListToProductMarketDtoList( product.getProductMarkets() ) );
        productDto.setQuantity( product.getQuantity() );

        return productDto;
    }

    @Override
    public Product toEntity(ProductDto dto) {
        if ( dto == null ) {
            return null;
        }

        Product product = new Product();

        product.setArticle( dto.getArticle() );
        product.setBarcode( dto.getBarcode() );
        product.setCategory( categoryMapper.categoryDtoToCategory( dto.getCategory() ) );
        product.setId( dto.getId() );
        byte[] image = dto.getImage();
        if ( image != null ) {
            product.setImage( Arrays.copyOf( image, image.length ) );
        }
        product.setImageUrl( dto.getImageUrl() );
        product.setName( dto.getName() );
        product.setPackageInfo( dto.getPackageInfo() );
        product.setPrice( dto.getPrice() );
        product.setProductAttributeValues( productAttributeValueDtoListToProductAttributeValueList( dto.getProductAttributeValues() ) );
        product.setProductMarkets( productMarketDtoListToProductMarketList( dto.getProductMarkets() ) );
        product.setQuantity( dto.getQuantity() );

        enrichPickupProductMarketReverseUrls( product );

        return product;
    }

    protected List<ProductAttributeValueDto> productAttributeValueListToProductAttributeValueDtoList(List<ProductAttributeValue> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductAttributeValueDto> list1 = new ArrayList<ProductAttributeValueDto>( list.size() );
        for ( ProductAttributeValue productAttributeValue : list ) {
            list1.add( productAttributeValueMapper.toDto( productAttributeValue ) );
        }

        return list1;
    }

    protected List<ProductMarketDto> productMarketListToProductMarketDtoList(List<ProductMarket> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductMarketDto> list1 = new ArrayList<ProductMarketDto>( list.size() );
        for ( ProductMarket productMarket : list ) {
            list1.add( productMarketMapper.toDto( productMarket ) );
        }

        return list1;
    }

    protected List<ProductAttributeValue> productAttributeValueDtoListToProductAttributeValueList(List<ProductAttributeValueDto> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductAttributeValue> list1 = new ArrayList<ProductAttributeValue>( list.size() );
        for ( ProductAttributeValueDto productAttributeValueDto : list ) {
            list1.add( productAttributeValueMapper.toEntity( productAttributeValueDto ) );
        }

        return list1;
    }

    protected List<ProductMarket> productMarketDtoListToProductMarketList(List<ProductMarketDto> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductMarket> list1 = new ArrayList<ProductMarket>( list.size() );
        for ( ProductMarketDto productMarketDto : list ) {
            list1.add( productMarketMapper.toEntity( productMarketDto ) );
        }

        return list1;
    }
}
