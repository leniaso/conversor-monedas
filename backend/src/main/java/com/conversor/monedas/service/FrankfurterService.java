package com.conversor.monedas.service;

import com.conversor.monedas.dto.FrankfurterLatestResponse;
import com.conversor.monedas.dto.FrankfurterTimeSeriesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FrankfurterService {

    private final WebClient frankfurterWebClient;

    /** Lista todas las monedas soportadas: codigo -> nombre completo */
    public Map<String, String> getCurrencies() {
        return frankfurterWebClient.get()
                .uri("/currencies")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    /** Todas las tasas más recientes a partir de una moneda base (para el ticker) */
    public FrankfurterLatestResponse getLatestRates(String base) {
        return frankfurterWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/latest")
                        .queryParam("base", base)
                        .build())
                .retrieve()
                .bodyToMono(FrankfurterLatestResponse.class)
                .block();
    }

    /** Tasa de cambio más reciente entre dos monedas */
    public FrankfurterLatestResponse getLatestRate(String from, String to) {
        return frankfurterWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/latest")
                        .queryParam("base", from)
                        .queryParam("symbols", to)
                        .build())
                .retrieve()
                .bodyToMono(FrankfurterLatestResponse.class)
                .block();
    }

    /** Serie histórica de tasas entre dos fechas, para la gráfica de variación */
    public FrankfurterTimeSeriesResponse getTimeSeries(String from, String to, LocalDate start, LocalDate end) {
        return frankfurterWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{start}..{end}")
                        .queryParam("base", from)
                        .queryParam("symbols", to)
                        .build(start, end))
                .retrieve()
                .bodyToMono(FrankfurterTimeSeriesResponse.class)
                .block();
    }
}
