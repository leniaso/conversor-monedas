package com.conversor.monedas.controller;

import com.conversor.monedas.dto.ConvertRequest;
import com.conversor.monedas.dto.ConvertResponse;
import com.conversor.monedas.dto.TimeSeriesResponse;
import com.conversor.monedas.model.ConversionHistory;
import com.conversor.monedas.service.ConversionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.closeTo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConversionService conversionService;

    @Test
    void getCurrencies_deberiaRetornarListaDeMonedas() throws Exception {
        // Arrange
        Map<String, String> currencies = Map.of("USD", "Dólar", "EUR", "Euro");
        when(conversionService.listCurrencies()).thenReturn(currencies);

        // Act
        ResultActions result = mockMvc.perform(get("/api/currencies"));

        // Assert
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.USD").value("Dólar"));
        verify(conversionService).listCurrencies();
    }

    @Test
    void getRates_conParametroPorDefecto_deberiaUsarUSD() throws Exception {
        // Arrange
        when(conversionService.listLatestRates("USD"))
                .thenReturn(Map.of("EUR", new BigDecimal("0.92")));

        // Act
        ResultActions result = mockMvc.perform(get("/api/rates"));

        // Assert
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.EUR").value(closeTo(0.92, 0.001)));
        verify(conversionService).listLatestRates("USD");
    }

    @Test
    void getRates_conBaseExplicita_deberiaUsarEseParametro() throws Exception {
        // Arrange
        when(conversionService.listLatestRates("COP"))
                .thenReturn(Map.of("USD", new BigDecimal("0.00025")));

        // Act
        ResultActions result = mockMvc.perform(get("/api/rates").param("base", "COP"));

        // Assert
        result.andExpect(status().isOk());
        verify(conversionService).listLatestRates("COP");
    }

    @Test
    void convert_conRequestValido_deberiaRetornarConversion() throws Exception {
        // Arrange
        ConvertRequest request = new ConvertRequest();
        request.setFrom("USD");
        request.setTo("EUR");
        request.setAmount(new BigDecimal("100"));

        ConvertResponse response = ConvertResponse.builder()
                .from("USD")
                .to("EUR")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("0.92"))
                .convertedAmount(new BigDecimal("92.00"))
                .date(LocalDate.of(2024, 1, 1))
                .build();

        when(conversionService.convert(any(ConvertRequest.class))).thenReturn(response);

        // Act
        ResultActions result = mockMvc.perform(post("/api/convert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.from").value("USD"))
              .andExpect(jsonPath("$.to").value("EUR"))
              .andExpect(jsonPath("$.convertedAmount").value(closeTo(92.00, 0.001)));
        verify(conversionService).convert(any(ConvertRequest.class));
    }

    @Test
    void convert_sinFrom_deberiaRetornar400() throws Exception {
        // Arrange
        ConvertRequest request = new ConvertRequest();
        request.setFrom(""); // viola @NotBlank
        request.setTo("EUR");
        request.setAmount(new BigDecimal("100"));

        // Act
        ResultActions result = mockMvc.perform(post("/api/convert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isBadRequest());
        verify(conversionService, never()).convert(any());
    }

    @Test
    void convert_sinTo_deberiaRetornar400() throws Exception {
        // Arrange
        ConvertRequest request = new ConvertRequest();
        request.setFrom("USD");
        request.setTo(""); // viola @NotBlank
        request.setAmount(new BigDecimal("100"));

        // Act
        ResultActions result = mockMvc.perform(post("/api/convert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isBadRequest());
        verify(conversionService, never()).convert(any());
    }

    @Test
    void convert_conAmountNulo_deberiaRetornar400() throws Exception {
        // Arrange
        ConvertRequest request = new ConvertRequest();
        request.setFrom("USD");
        request.setTo("EUR");
        request.setAmount(null); // viola @NotNull

        // Act
        ResultActions result = mockMvc.perform(post("/api/convert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isBadRequest());
        verify(conversionService, never()).convert(any());
    }

    @Test
    void convert_conAmountCeroOMenor_deberiaRetornar400() throws Exception {
        // Arrange
        ConvertRequest request = new ConvertRequest();
        request.setFrom("USD");
        request.setTo("EUR");
        request.setAmount(new BigDecimal("0")); // viola @DecimalMin(inclusive = false)

        // Act
        ResultActions result = mockMvc.perform(post("/api/convert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isBadRequest());
        verify(conversionService, never()).convert(any());
    }

    @Test
    void convert_conJsonVacio_deberiaRetornar400() throws Exception {
        // Arrange
        // Ningún campo presente -> dispara las 3 validaciones (@NotBlank x2, @NotNull)
        String jsonVacio = "{}";

        // Act
        ResultActions result = mockMvc.perform(post("/api/convert")
                .contentType("application/json")
                .content(jsonVacio));

        // Assert
        result.andExpect(status().isBadRequest());
        verify(conversionService, never()).convert(any());
    }

    @Test
    void getHistory_conParametrosPorDefecto_deberiaUsarPage0Size10() throws Exception {
        // Arrange
        Page<ConversionHistory> page = new PageImpl<>(List.of());
        when(conversionService.getHistory(0, 10)).thenReturn(page);

        // Act
        ResultActions result = mockMvc.perform(get("/api/history"));

        // Assert
        result.andExpect(status().isOk());
        verify(conversionService).getHistory(0, 10);
    }

    @Test
    void getVariation_sinFechas_deberiaUsarUltimos30Dias() throws Exception {
        // Arrange
        TimeSeriesResponse response = new TimeSeriesResponse();
        when(conversionService.getVariation(eq("USD"), eq("EUR"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        // Act
        ResultActions result = mockMvc.perform(get("/api/variation")
                .param("from", "USD")
                .param("to", "EUR"));

        // Assert
        result.andExpect(status().isOk());
        verify(conversionService).getVariation(eq("USD"), eq("EUR"), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getVariation_conFechasExplicitas_deberiaUsarlas() throws Exception {
        // Arrange
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 2, 1);
        TimeSeriesResponse response = new TimeSeriesResponse();

        when(conversionService.getVariation("USD", "EUR", start, end)).thenReturn(response);

        // Act
        ResultActions result = mockMvc.perform(get("/api/variation")
                .param("from", "USD")
                .param("to", "EUR")
                .param("start", "2024-01-01")
                .param("end", "2024-02-01"));

        // Assert
        result.andExpect(status().isOk());
        verify(conversionService).getVariation("USD", "EUR", start, end);
    }

    @Test
    void getVariation_sinFrom_deberiaRetornar400() throws Exception {
        // Arrange
        // "from" es @RequestParam obligatorio (sin defaultValue) -> no se envía

        // Act
        ResultActions result = mockMvc.perform(get("/api/variation")
                .param("to", "EUR"));

        // Assert
        result.andExpect(status().isBadRequest());
        verify(conversionService, never()).getVariation(any(), any(), any(), any());
    }

    @Test
    void getVariation_sinTo_deberiaRetornar400() throws Exception {
        // Arrange
        // "to" es @RequestParam obligatorio (sin defaultValue) -> no se envía

        // Act
        ResultActions result = mockMvc.perform(get("/api/variation")
                .param("from", "USD"));

        // Assert
        result.andExpect(status().isBadRequest());
        verify(conversionService, never()).getVariation(any(), any(), any(), any());
    }

    @Test
    void getVariation_sinFromNiTo_deberiaRetornar400() throws Exception {
        // Arrange
        // Ninguno de los 2 parámetros obligatorios se envía

        // Act
        ResultActions result = mockMvc.perform(get("/api/variation"));

        // Assert
        result.andExpect(status().isBadRequest());
        verify(conversionService, never()).getVariation(any(), any(), any(), any());
    }

    @Test
    void convert_cuandoServiceLanzaIllegalArgument_deberiaRetornar400ConMensaje() throws Exception {
        // Arrange
        ConvertRequest request = new ConvertRequest();
        request.setFrom("USD");
        request.setTo("XYZ"); // moneda no soportada por el service
        request.setAmount(new BigDecimal("100"));

        when(conversionService.convert(any(ConvertRequest.class)))
                .thenThrow(new IllegalArgumentException("Moneda no soportada: XYZ"));

        // Act
        ResultActions result = mockMvc.perform(post("/api/convert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        // GlobalExceptionHandler.handleIllegalArgument -> 400 con {timestamp, error}
        result.andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.error").value("Moneda no soportada: XYZ"))
              .andExpect(jsonPath("$.timestamp").exists());
        verify(conversionService).convert(any(ConvertRequest.class));
    }

    @Test
    void convert_cuandoServiceLanzaExcepcionGenerica_deberiaRetornar500ConMensaje() throws Exception {
        // Arrange
        ConvertRequest request = new ConvertRequest();
        request.setFrom("USD");
        request.setTo("EUR");
        request.setAmount(new BigDecimal("100"));

        when(conversionService.convert(any(ConvertRequest.class)))
                .thenThrow(new RuntimeException("Fallo al conectar con Frankfurter API"));

        // Act
        ResultActions result = mockMvc.perform(post("/api/convert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        // GlobalExceptionHandler.handleGeneric -> 500 con {timestamp, error: "Error inesperado: ..."}
        result.andExpect(status().isInternalServerError())
              .andExpect(jsonPath("$.error").value("Error inesperado: Fallo al conectar con Frankfurter API"))
              .andExpect(jsonPath("$.timestamp").exists());
        verify(conversionService).convert(any(ConvertRequest.class));
    }

    @Test
    void getRates_cuandoServiceLanzaIllegalArgument_deberiaRetornar400() throws Exception {
        // Arrange
        when(conversionService.listLatestRates("XYZ"))
                .thenThrow(new IllegalArgumentException("Moneda base no soportada: XYZ"));

        // Act
        ResultActions result = mockMvc.perform(get("/api/rates").param("base", "XYZ"));

        // Assert
        result.andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.error").value("Moneda base no soportada: XYZ"));
        verify(conversionService).listLatestRates("XYZ");
    }

    @Test
    void getRates_cuandoServiceLanzaExcepcionGenerica_deberiaRetornar500() throws Exception {
        // Arrange
        when(conversionService.listLatestRates("USD"))
                .thenThrow(new RuntimeException("Servicio externo no disponible"));

        // Act
        ResultActions result = mockMvc.perform(get("/api/rates").param("base", "USD"));

        // Assert
        result.andExpect(status().isInternalServerError())
              .andExpect(jsonPath("$.error").value("Error inesperado: Servicio externo no disponible"));
        verify(conversionService).listLatestRates("USD");
    }

    @Test
    void convert_conRequestInvalido_deberiaRetornar400ConMensajeDeCampo() throws Exception {
        // Arrange
        // "from" vacío -> dispara @NotBlank -> MethodArgumentNotValidException
        // -> capturada por GlobalExceptionHandler.handleValidation
        ConvertRequest request = new ConvertRequest();
        request.setFrom("");
        request.setTo("EUR");
        request.setAmount(new BigDecimal("100"));

        // Act
        ResultActions result = mockMvc.perform(post("/api/convert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.error").value("from: La moneda de origen es obligatoria"))
              .andExpect(jsonPath("$.timestamp").exists());
        verify(conversionService, never()).convert(any());
    }
}