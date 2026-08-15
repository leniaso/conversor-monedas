package com.conversor.monedas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrenciesResponse {
    private Map<String, String> currencies; // codigo -> nombre, ej. "USD" -> "US Dollar"
}
