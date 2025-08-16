package ru.dmitartur.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для обновления количества товара на складе
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateDto {
    private int quantityChange;
}
