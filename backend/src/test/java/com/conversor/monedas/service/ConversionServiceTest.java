package com.conversor.monedas.service;

import com.conversor.monedas.dto.ConvertRequest;
import com.conversor.monedas.dto.ConvertResponse;
import com.conversor.monedas.dto.FrankfurterLatestResponse;
import com.conversor.monedas.dto.FrankfurterTimeSeriesResponse;
import com.conversor.monedas.dto.TimeSeriesResponse;
import com.conversor.monedas.model.ConversionHistory;
import com.conversor.monedas.repository.ConversionHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de ConversionService, siguiendo el patrón AAA
 * (Arrange - Act - Assert) en cada test.
 *
 * FrankfurterService y ConversionHistoryRepository se simulan con Mockito
 * para probar la lógica de negocio de forma completamente aislada,
 * sin llamadas HTTP reales ni conexión a la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class ConversionServiceTest {

    @Mock
    private FrankfurterService frankfurterService;

    @Mock
    private ConversionHistoryRepository historyRepository;

    @InjectMocks
    private ConversionService conversionService;

    // ---------------------------------------------------------------
    // listCurrencies()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("listCurrencies debe retornar el mapa de monedas provisto por FrankfurterService")
    void listCurrencies_debeRetornarMonedasDeFrankfurterService() {
        // Arrange
        Map<String, String> monedasEsperadas = new LinkedHashMap<>();
        monedasEsperadas.put("USD", "US Dollar");
        monedasEsperadas.put("EUR", "Euro");
        when(frankfurterService.getCurrencies()).thenReturn(monedasEsperadas);

        // Act
        Map<String, String> resultado = conversionService.listCurrencies();

        // Assert
        assertThat(resultado).isEqualTo(monedasEsperadas);
        verify(frankfurterService, times(1)).getCurrencies();
    }

    // ---------------------------------------------------------------
    // listLatestRates()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("listLatestRates debe convertir la base a mayúsculas antes de consultar")
    void listLatestRates_debeConvertirBaseAMayusculas() {
        // Arrange
        String baseMinuscula = "usd";
        Map<String, BigDecimal> tasas = Map.of("EUR", new BigDecimal("0.92"));
        FrankfurterLatestResponse respuestaExterna = new FrankfurterLatestResponse();
        respuestaExterna.base = "USD";
        respuestaExterna.rates = tasas;
        when(frankfurterService.getLatestRates("USD")).thenReturn(respuestaExterna);

        // Act
        Map<String, BigDecimal> resultado = conversionService.listLatestRates(baseMinuscula);

        // Assert
        assertThat(resultado).isEqualTo(tasas);
        verify(frankfurterService).getLatestRates("USD");
    }

    // ---------------------------------------------------------------
    // convert()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("convert debe calcular el monto convertido y guardar el historial")
    void convert_debeCalcularMontoConvertidoYGuardarHistorial() {
        // Arrange
        ConvertRequest request = new ConvertRequest();
        request.setFrom("usd");
        request.setTo("eur");
        request.setAmount(new BigDecimal("100"));

        FrankfurterLatestResponse respuestaExterna = new FrankfurterLatestResponse();
        respuestaExterna.date = LocalDate.of(2026, 8, 15);
        respuestaExterna.rates = Map.of("EUR", new BigDecimal("0.9200"));
        when(frankfurterService.getLatestRate("USD", "EUR")).thenReturn(respuestaExterna);

        // Act
        ConvertResponse resultado = conversionService.convert(request);

        // Assert: la respuesta trae el cálculo correcto
        assertThat(resultado.getFrom()).isEqualTo("USD");
        assertThat(resultado.getTo()).isEqualTo("EUR");
        assertThat(resultado.getRate()).isEqualByComparingTo("0.9200");
        assertThat(resultado.getConvertedAmount()).isEqualByComparingTo("92.0000");
        assertThat(resultado.getDate()).isEqualTo(LocalDate.of(2026, 8, 15));

        // Assert: se guardó exactamente una vez en el historial, con los datos correctos
        ArgumentCaptor<ConversionHistory> captor = ArgumentCaptor.forClass(ConversionHistory.class);
        verify(historyRepository, times(1)).save(captor.capture());
        ConversionHistory guardado = captor.getValue();
        assertThat(guardado.getFromCurrency()).isEqualTo("USD");
        assertThat(guardado.getToCurrency()).isEqualTo("EUR");
        assertThat(guardado.getAmount()).isEqualByComparingTo("100");
        assertThat(guardado.getConvertedAmount()).isEqualByComparingTo("92.0000");
    }

    @Test
    @DisplayName("convert debe lanzar IllegalArgumentException si Frankfurter no retorna tasa para la moneda destino")
    void convert_debeLanzarExcepcionSiNoHayTasaDisponible() {
        // Arrange
        ConvertRequest request = new ConvertRequest();
        request.setFrom("USD");
        request.setTo("XYZ"); // moneda inexistente
        request.setAmount(new BigDecimal("50"));

        FrankfurterLatestResponse respuestaExterna = new FrankfurterLatestResponse();
        respuestaExterna.rates = Map.of(); // no trae la moneda pedida
        when(frankfurterService.getLatestRate("USD", "XYZ")).thenReturn(respuestaExterna);

        // Act & Assert
        assertThatThrownBy(() -> conversionService.convert(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("USD")
                .hasMessageContaining("XYZ");

        // Assert: al fallar, nunca debe intentar guardar en el historial
        verify(historyRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // getHistory()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getHistory debe delegar en el repositorio con la página y tamaño solicitados")
    void getHistory_debeDelegarEnRepositorioConPaginacion() {
        // Arrange
        ConversionHistory item = ConversionHistory.builder()
                .fromCurrency("USD")
                .toCurrency("EUR")
                .amount(new BigDecimal("10"))
                .rate(new BigDecimal("0.9"))
                .convertedAmount(new BigDecimal("9"))
                .build();
        Page<ConversionHistory> paginaEsperada = new PageImpl<>(List.of(item));
        when(historyRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(paginaEsperada);

        // Act
        Page<ConversionHistory> resultado = conversionService.getHistory(0, 10);

        // Assert
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getFromCurrency()).isEqualTo("USD");
        verify(historyRepository).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    // ---------------------------------------------------------------
    // getVariation()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getVariation debe transformar y ordenar por fecha los puntos de la serie histórica")
    void getVariation_debeRetornarPuntosOrdenadosPorFecha() {
        // Arrange
        LocalDate dia1 = LocalDate.of(2026, 8, 1);
        LocalDate dia2 = LocalDate.of(2026, 8, 2);
        LocalDate dia3 = LocalDate.of(2026, 8, 3);

        // Se insertan intencionalmente desordenadas para probar el ordenamiento
        Map<LocalDate, Map<String, BigDecimal>> ratesPorFecha = new LinkedHashMap<>();
        ratesPorFecha.put(dia3, Map.of("EUR", new BigDecimal("0.93")));
        ratesPorFecha.put(dia1, Map.of("EUR", new BigDecimal("0.91")));
        ratesPorFecha.put(dia2, Map.of("EUR", new BigDecimal("0.92")));

        FrankfurterTimeSeriesResponse serieExterna = new FrankfurterTimeSeriesResponse();
        serieExterna.rates = ratesPorFecha;
        when(frankfurterService.getTimeSeries("USD", "EUR", dia1, dia3)).thenReturn(serieExterna);

        // Act
        TimeSeriesResponse resultado = conversionService.getVariation("usd", "eur", dia1, dia3);

        // Assert
        assertThat(resultado.getFrom()).isEqualTo("USD");
        assertThat(resultado.getTo()).isEqualTo("EUR");
        assertThat(resultado.getPoints()).hasSize(3);
        assertThat(resultado.getPoints().get(0).getDate()).isEqualTo(dia1);
        assertThat(resultado.getPoints().get(1).getDate()).isEqualTo(dia2);
        assertThat(resultado.getPoints().get(2).getDate()).isEqualTo(dia3);
        assertThat(resultado.getPoints().get(0).getRate()).isEqualByComparingTo("0.91");
    }
}
