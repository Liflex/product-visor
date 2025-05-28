package org.example.dto;

import jakarta.persistence.*;
import lombok.Data;
import org.example.entity.Attribute;

import java.util.List;
import java.util.Set;

@Data
public class CategoryDto {
    private Long id;
    private String name;

    private List<AttributeDto> attributes;
}
