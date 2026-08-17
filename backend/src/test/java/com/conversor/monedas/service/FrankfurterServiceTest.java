package com.conversor.monedas.service;

import com.conversor.monedas.dto.FrankfurterLatestResponse;
import com.conversor.monedas.dto.FrankfurterTimeSeriesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de FrankfurterService, siguiendo el patrón AAA.
 *
 * WebClient se simula manualmente en cada paso de su API fluida
 * (get -> uri -> retrieve -> bodyToMono -> block), para no depender
 * de una llamada HTTP real a la API de Frankfurter durante los tests.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class FrankfurterServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private FrankfurterService frankfurterService;

    @BeforeEach
    void setUp() {
        frankfurterService = new FrankfurterService(webClient);
    }

    // ---------------------------------------------------------------
    // getCurrencies()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getCurrencies debe retornar el mapa de monedas que entrega Frankfurter")
    void getCurrencies_debeRetornarMapaDeMonedas() {
        // Arrange
        Map<String, String> monedasEsperadas = Map.of("USD", "US Dollar", "EUR", "Euro");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/currencies")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(monedasEsperadas));

        // Act
        Map<String, String> resultado = frankfurterService.getCurrencies();

        // Assert
        assertThat(resultado).isEqualTo(monedasEsperadas);
    }

    // ---------------------------------------------------------------
    // getLatestRates()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getLatestRates debe retornar las tasas para la moneda base solicitada")
    void getLatestRates_debeRetornarTasasParaBase() {
        // Arrange
        FrankfurterLatestResponse respuestaEsperada = new FrankfurterLatestResponse();
        respuestaEsperada.base = "USD";
        respuestaEsperada.rates = Map.of("EUR", new BigDecimal("0.92"), "GBP", new BigDecimal("0.78"));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(FrankfurterLatestResponse.class))
                .thenReturn(Mono.just(respuestaEsperada));

        // Act
        FrankfurterLatestResponse resultado = frankfurterService.getLatestRates("USD");

        // Assert
        assertThat(resultado.base).isEqualTo("USD");
        assertThat(resultado.rates).containsEntry("EUR", new BigDecimal("0.92"));
    }

    // ---------------------------------------------------------------
    // getLatestRate()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getLatestRate debe retornar la tasa entre dos monedas específicas")
    void getLatestRate_debeRetornarTasaEntreDosMonedas() {
        // Arrange
        FrankfurterLatestResponse respuestaEsperada = new FrankfurterLatestResponse();
        respuestaEsperada.date = LocalDate.of(2026, 8, 15);
        respuestaEsperada.rates = Map.of("EUR", new BigDecimal("0.9200"));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(FrankfurterLatestResponse.class))
                .thenReturn(Mono.just(respuestaEsperada));

        // Act
        FrankfurterLatestResponse resultado = frankfurterService.getLatestRate("USD", "EUR");

        // Assert
        assertThat(resultado.date).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(resultado.rates.get("EUR")).isEqualByComparingTo("0.9200");
    }

    // ---------------------------------------------------------------
    // getTimeSeries()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getTimeSeries debe retornar la serie histórica entre dos fechas")
    void getTimeSeries_debeRetornarSerieHistorica() {
        // Arrange
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 15);

        FrankfurterTimeSeriesResponse respuestaEsperada = new FrankfurterTimeSeriesResponse();
        respuestaEsperada.start_date = start;
        respuestaEsperada.end_date = end;
        respuestaEsperada.rates = Map.of(start, Map.of("EUR", new BigDecimal("0.91")));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(FrankfurterTimeSeriesResponse.class))
                .thenReturn(Mono.just(respuestaEsperada));

        // Act
        FrankfurterTimeSeriesResponse resultado = frankfurterService.getTimeSeries("USD", "EUR", start, end);

        // Assert
        assertThat(resultado.start_date).isEqualTo(start);
        assertThat(resultado.end_date).isEqualTo(end);
        assertThat(resultado.rates.get(start)).containsEntry("EUR", new BigDecimal("0.91"));
    }
}
