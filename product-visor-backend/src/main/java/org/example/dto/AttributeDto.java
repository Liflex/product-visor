package org.example.dto;

import lombok.Data;

@Data
public class AttributeDto {

    private Long id;
    private String name;
    private String nameRus;
    private String type;
    private boolean required;
    private boolean multiple;
}
