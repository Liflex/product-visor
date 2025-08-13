package ru.dmitartur.dto;

import lombok.Data;
import java.util.List;

@Data
public class CategoryDto {
    private Long id;
    private String name;

    private List<AttributeDto> attributes;
}
