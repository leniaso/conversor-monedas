package com.conversor.monedas.controller;

import com.conversor.monedas.dto.ConvertRequest;
import com.conversor.monedas.dto.ConvertResponse;
import com.conversor.monedas.dto.TimeSeriesResponse;
import com.conversor.monedas.model.ConversionHistory;
import com.conversor.monedas.service.ConversionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CurrencyController {

    private final ConversionService conversionService;

    // GET /api/currencies -> lista de monedas soportadas
    @GetMapping("/currencies")
    public ResponseEntity<Map<String, String>> getCurrencies() {
        return ResponseEntity.ok(conversionService.listCurrencies());
    }

    // GET /api/rates?base=USD -> tasas actuales para el ticker (no guarda historial)
    @GetMapping("/rates")
    public ResponseEntity<Map<String, java.math.BigDecimal>> getRates(
            @RequestParam(defaultValue = "USD") String base) {
        return ResponseEntity.ok(conversionService.listLatestRates(base));
    }

    // POST /api/convert -> convierte y guarda en el historial
    @PostMapping("/convert")
    public ResponseEntity<ConvertResponse> convert(@Valid @RequestBody ConvertRequest request) {
        return ResponseEntity.ok(conversionService.convert(request));
    }

    // GET /api/history?page=0&size=10 -> historial de conversiones guardadas (Neon)
    @GetMapping("/history")
    public ResponseEntity<Page<ConversionHistory>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(conversionService.getHistory(page, size));
    }

    // GET /api/variation?from=USD&to=EUR&start=2024-01-01&end=2024-02-01 -> datos para la gráfica
    @GetMapping("/variation")
    public ResponseEntity<TimeSeriesResponse> getVariation(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        LocalDate effectiveEnd = end != null ? end : LocalDate.now();
        LocalDate effectiveStart = start != null ? start : effectiveEnd.minusDays(30);

        return ResponseEntity.ok(conversionService.getVariation(from, to, effectiveStart, effectiveEnd));
    }
}
