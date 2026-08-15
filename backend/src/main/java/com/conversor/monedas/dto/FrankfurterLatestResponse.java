package com.conversor.monedas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

// Mapea la respuesta de https://api.frankfurter.dev/v1/latest?from=USD&to=EUR
public class FrankfurterLatestResponse {
    public BigDecimal amount;
    public String base;
    public LocalDate date;
    public Map<String, BigDecimal> rates;
}
