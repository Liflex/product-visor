package org.example.mapper;
import org.example.dto.AttributeDto;
import org.example.dto.ProductDto;
import org.example.entity.Attribute;
import org.example.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", uses = {ProductAttributeValueMapper.class})
@Component
public interface ProductMapper {

    ProductDto toDto(Product product);

    Product toEntity(ProductDto productDto);
}
