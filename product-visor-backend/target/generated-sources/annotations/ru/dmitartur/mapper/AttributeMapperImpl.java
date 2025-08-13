package ru.dmitartur.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.dmitartur.dto.AttributeDto;
import ru.dmitartur.entity.Attribute;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-14T00:54:23+0300",
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
