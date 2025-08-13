package ru.dmitartur.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.dmitartur.dto.ProductAttributeValueDto;
import ru.dmitartur.dto.ProductDto;
import ru.dmitartur.dto.ProductMarketDto;
import ru.dmitartur.entity.Product;
import ru.dmitartur.entity.ProductAttributeValue;
import ru.dmitartur.entity.ProductMarket;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-14T00:54:23+0300",
    comments = "version: 1.6.2, compiler: javac, environment: Java 17.0.14 (Azul Systems, Inc.)"
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

        productDto.setId( product.getId() );
        productDto.setName( product.getName() );
        productDto.setArticle( product.getArticle() );
        productDto.setImageUrl( product.getImageUrl() );
        byte[] image = product.getImage();
        if ( image != null ) {
            productDto.setImage( Arrays.copyOf( image, image.length ) );
        }
        productDto.setBarcode( product.getBarcode() );
        productDto.setQuantity( product.getQuantity() );
        productDto.setCategory( categoryMapper.categoryToCategoryDto( product.getCategory() ) );
        productDto.setPrice( product.getPrice() );
        productDto.setPackageInfo( product.getPackageInfo() );
        productDto.setProductAttributeValues( productAttributeValueListToProductAttributeValueDtoList( product.getProductAttributeValues() ) );
        productDto.setProductMarkets( productMarketListToProductMarketDtoList( product.getProductMarkets() ) );

        return productDto;
    }

    @Override
    public Product toEntity(ProductDto dto) {
        if ( dto == null ) {
            return null;
        }

        Product product = new Product();

        product.setId( dto.getId() );
        product.setName( dto.getName() );
        product.setArticle( dto.getArticle() );
        byte[] image = dto.getImage();
        if ( image != null ) {
            product.setImage( Arrays.copyOf( image, image.length ) );
        }
        product.setImageUrl( dto.getImageUrl() );
        product.setBarcode( dto.getBarcode() );
        product.setQuantity( dto.getQuantity() );
        product.setCategory( categoryMapper.categoryDtoToCategory( dto.getCategory() ) );
        product.setPrice( dto.getPrice() );
        product.setPackageInfo( dto.getPackageInfo() );
        product.setProductAttributeValues( productAttributeValueDtoListToProductAttributeValueList( dto.getProductAttributeValues() ) );
        product.setProductMarkets( productMarketDtoListToProductMarketList( dto.getProductMarkets() ) );

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
