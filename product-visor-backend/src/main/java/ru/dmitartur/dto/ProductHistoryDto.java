package ru.dmitartur.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO для истории изменений продукта
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductHistoryDto {
    private Long id;
    private Long productId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String changeReason;
    private String sourceSystem;
    private String sourceId;
    private OffsetDateTime createdAt;
    private String metadata;
}
