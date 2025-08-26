package ru.dmitartur.common.dto.marketplace;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO для указания диапазона дат
 */
@Setter
@Getter
public class DateRangeDto {
    private String from;
    private String to;

    public DateRangeDto() {}

    public DateRangeDto(String from, String to) {
        this.from = from;
        this.to = to;
    }
}
