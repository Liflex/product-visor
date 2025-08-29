package ru.dmitartur.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private UUID userId;
    private UUID companyId;
    private LocalDateTime createdAt;
    private String metadata;
}
