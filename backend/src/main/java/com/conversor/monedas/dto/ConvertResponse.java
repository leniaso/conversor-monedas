package com.conversor.monedas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertResponse {
    private String from;
    private String to;
    private BigDecimal amount;
    private BigDecimal rate;
    private BigDecimal convertedAmount;
    private LocalDate date;
}
