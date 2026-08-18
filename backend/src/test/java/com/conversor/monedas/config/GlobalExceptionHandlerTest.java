package com.conversor.monedas.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de GlobalExceptionHandler, siguiendo el patrón AAA
 * (Arrange - Act - Assert) en cada test.
 *
 * BindingResult y MethodArgumentNotValidException se simulan con Mockito
 * porque son difíciles de construir directamente (requieren un contexto
 * real de validación), pero el resto del handler se prueba de forma
 * completamente aislada, sin levantar el contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    // ---------------------------------------------------------------
    // handleIllegalArgument()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("handleIllegalArgument debe retornar 400 con el mensaje de la excepción")
    void handleIllegalArgument_debeRetornar400ConMensajeDeLaExcepcion() {
        // Arrange
        IllegalArgumentException ex = new IllegalArgumentException("No se encontró la tasa para USD -> XYZ");

        // Act
        ResponseEntity<Map<String, Object>> resultado = exceptionHandler.handleIllegalArgument(ex);

        // Assert
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resultado.getBody()).isNotNull();
        assertThat(resultado.getBody()).containsEntry("error", "No se encontró la tasa para USD -> XYZ");
        assertThat(resultado.getBody()).containsKey("timestamp");
    }

    // ---------------------------------------------------------------
    // handleValidation()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("handleValidation debe retornar 400 con el primer error de campo formateado como 'campo: mensaje'")
    void handleValidation_debeRetornarPrimerErrorDeCampo() {
        // Arrange
        FieldError fieldError = new FieldError("convertRequest", "amount", "El monto debe ser mayor a 0");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<Map<String, Object>> resultado = exceptionHandler.handleValidation(methodArgumentNotValidException);

        // Assert
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resultado.getBody()).isNotNull();
        assertThat(resultado.getBody()).containsEntry("error", "amount: El monto debe ser mayor a 0");
        assertThat(resultado.getBody()).containsKey("timestamp");
    }

    @Test
    @DisplayName("handleValidation debe usar el mensaje por defecto 'Datos inválidos' si no hay errores de campo")
    void handleValidation_debeUsarMensajePorDefectoSiNoHayErroresDeCampo() {
        // Arrange
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // Act
        ResponseEntity<Map<String, Object>> resultado = exceptionHandler.handleValidation(methodArgumentNotValidException);

        // Assert
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resultado.getBody()).isNotNull();
        assertThat(resultado.getBody()).containsEntry("error", "Datos inválidos");
    }

    @Test
    @DisplayName("handleValidation debe tomar solo el primer error cuando hay varios errores de campo")
    void handleValidation_debeTomarSoloElPrimerErrorCuandoHayVarios() {
        // Arrange
        FieldError primerError = new FieldError("convertRequest", "from", "La moneda de origen es obligatoria");
        FieldError segundoError = new FieldError("convertRequest", "to", "La moneda de destino es obligatoria");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(primerError, segundoError));

        // Act
        ResponseEntity<Map<String, Object>> resultado = exceptionHandler.handleValidation(methodArgumentNotValidException);

        // Assert
        assertThat(resultado.getBody()).containsEntry("error", "from: La moneda de origen es obligatoria");
    }

    // ---------------------------------------------------------------
    // handleGeneric()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("handleGeneric debe retornar 500 con el mensaje de la excepción prefijado con 'Error inesperado:'")
    void handleGeneric_debeRetornar500ConMensajePrefijado() {
        // Arrange
        Exception ex = new RuntimeException("Fallo de conexión con Frankfurter");

        // Act
        ResponseEntity<Map<String, Object>> resultado = exceptionHandler.handleGeneric(ex);

        // Assert
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resultado.getBody()).isNotNull();
        assertThat(resultado.getBody()).containsEntry("error", "Error inesperado: Fallo de conexión con Frankfurter");
        assertThat(resultado.getBody()).containsKey("timestamp");
    }
}
