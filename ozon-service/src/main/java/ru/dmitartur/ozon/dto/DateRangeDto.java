package ru.dmitartur.ozon.dto;

import lombok.Getter;
import lombok.Setter;

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


