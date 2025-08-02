package org.example.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.example.dto.AttributeDto;
import org.example.dto.CategoryDto;
import org.example.entity.Attribute;
import org.example.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-02T16:34:32+0300",
    comments = "version: 1.6.2, compiler: Eclipse JDT (IDE) 3.42.50.v20250628-1110, environment: Java 21.0.7 (Azul Systems, Inc.)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Autowired
    private AttributeMapper attributeMapper;

    @Override
    public CategoryDto categoryToCategoryDto(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryDto categoryDto = new CategoryDto();

        categoryDto.setAttributes( attributeListToAttributeDtoList( category.getAttributes() ) );
        categoryDto.setId( category.getId() );
        categoryDto.setName( category.getName() );

        return categoryDto;
    }

    @Override
    public Category categoryDtoToCategory(CategoryDto categoryDto) {
        if ( categoryDto == null ) {
            return null;
        }

        Category category = new Category();

        category.setAttributes( attributeDtoListToAttributeList( categoryDto.getAttributes() ) );
        category.setId( categoryDto.getId() );
        category.setName( categoryDto.getName() );

        return category;
    }

    protected List<AttributeDto> attributeListToAttributeDtoList(List<Attribute> list) {
        if ( list == null ) {
            return null;
        }

        List<AttributeDto> list1 = new ArrayList<AttributeDto>( list.size() );
        for ( Attribute attribute : list ) {
            list1.add( attributeMapper.toDto( attribute ) );
        }

        return list1;
    }

    protected List<Attribute> attributeDtoListToAttributeList(List<AttributeDto> list) {
        if ( list == null ) {
            return null;
        }

        List<Attribute> list1 = new ArrayList<Attribute>( list.size() );
        for ( AttributeDto attributeDto : list ) {
            list1.add( attributeMapper.toEntity( attributeDto ) );
        }

        return list1;
    }
}
