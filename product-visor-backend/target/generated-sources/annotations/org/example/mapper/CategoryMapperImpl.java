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
    date = "2025-07-12T22:47:32+0300",
    comments = "version: 1.6.2, compiler: javac, environment: Java 17.0.14 (Azul Systems, Inc.)"
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

        categoryDto.setId( category.getId() );
        categoryDto.setName( category.getName() );
        categoryDto.setAttributes( attributeListToAttributeDtoList( category.getAttributes() ) );

        return categoryDto;
    }

    @Override
    public Category categoryDtoToCategory(CategoryDto categoryDto) {
        if ( categoryDto == null ) {
            return null;
        }

        Category category = new Category();

        category.setId( categoryDto.getId() );
        category.setName( categoryDto.getName() );
        category.setAttributes( attributeDtoListToAttributeList( categoryDto.getAttributes() ) );

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
