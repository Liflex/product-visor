package org.example.mapper;

import javax.annotation.processing.Generated;
import org.example.dto.AttributeDto;
import org.example.entity.Attribute;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-13T20:56:49+0300",
    comments = "version: 1.6.2, compiler: javac, environment: Java 17.0.14 (Azul Systems, Inc.)"
)
@Component
public class AttributeMapperImpl implements AttributeMapper {

    @Override
    public AttributeDto toDto(Attribute attribute) {
        if ( attribute == null ) {
            return null;
        }

        AttributeDto attributeDto = new AttributeDto();

        attributeDto.setId( attribute.getId() );
        attributeDto.setName( attribute.getName() );
        attributeDto.setNameRus( attribute.getNameRus() );
        attributeDto.setType( attribute.getType() );
        attributeDto.setRequired( attribute.isRequired() );
        attributeDto.setMultiple( attribute.isMultiple() );

        return attributeDto;
    }

    @Override
    public Attribute toEntity(AttributeDto attributeDto) {
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
}
