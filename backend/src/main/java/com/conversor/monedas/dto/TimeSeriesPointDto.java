package com.conversor.monedas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesPointDto {
    private LocalDate date;
    private BigDecimal rate;
}
