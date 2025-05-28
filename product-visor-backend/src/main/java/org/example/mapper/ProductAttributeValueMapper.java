package org.example.mapper;
import org.example.dto.AttributeDto;
import org.example.dto.ProductAttributeValueDto;
import org.example.entity.Attribute;
import org.example.entity.ProductAttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface ProductAttributeValueMapper {

    @Mapping(target = "productId", source = "product.id")
    ProductAttributeValueDto toDto(ProductAttributeValue attribute);

    @Mapping(target = "product.id", source = "productId")
    ProductAttributeValue toEntity(ProductAttributeValueDto attributeDto);
}
