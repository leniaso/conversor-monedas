package com.conversor.monedas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

// Mapea la respuesta de https://api.frankfurter.dev/v1/2024-01-01..2024-02-01?from=USD&to=EUR
public class FrankfurterTimeSeriesResponse {
    public String base;
    public LocalDate start_date;
    public LocalDate end_date;
    public Map<LocalDate, Map<String, BigDecimal>> rates;
}
