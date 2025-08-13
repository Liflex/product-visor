package ru.dmitartur.mapper;
import ru.dmitartur.dto.AttributeDto;
import ru.dmitartur.dto.CategoryDto;
import ru.dmitartur.entity.Attribute;
import ru.dmitartur.entity.Category;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface AttributeMapper {

    AttributeDto toDto(Attribute attribute);

    Attribute toEntity(AttributeDto attributeDto);
}
