package org.example.mapper;

import javax.annotation.processing.Generated;
import org.example.dto.AttributeDto;
import org.example.entity.Attribute;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-02T16:34:33+0300",
    comments = "version: 1.6.2, compiler: Eclipse JDT (IDE) 3.42.50.v20250628-1110, environment: Java 21.0.7 (Azul Systems, Inc.)"
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
        attributeDto.setMultiple( attribute.isMultiple() );
        attributeDto.setName( attribute.getName() );
        attributeDto.setNameRus( attribute.getNameRus() );
        attributeDto.setRequired( attribute.isRequired() );
        attributeDto.setType( attribute.getType() );

        return attributeDto;
    }

    @Override
    public Attribute toEntity(AttributeDto attributeDto) {
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
}
