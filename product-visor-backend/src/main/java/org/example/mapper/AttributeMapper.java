package org.example.mapper;
import org.example.dto.AttributeDto;
import org.example.dto.CategoryDto;
import org.example.entity.Attribute;
import org.example.entity.Category;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface AttributeMapper {

    AttributeDto toDto(Attribute attribute);

    Attribute toEntity(AttributeDto attributeDto);
}
