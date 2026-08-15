package com.conversor.monedas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConvertRequest {

    @NotBlank(message = "La moneda de origen es obligatoria")
    private String from;

    @NotBlank(message = "La moneda de destino es obligatoria")
    private String to;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
    private BigDecimal amount;
}
