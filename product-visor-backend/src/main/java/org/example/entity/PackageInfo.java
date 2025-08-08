package org.example.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class PackageInfo {
    private Double width; // ширина в см
    private Double height; // высота в см
    private Double length; // длина в см
    private Double weight; // вес упаковки в граммах
    private Integer quantityInPackage; // количество в упаковке
} 