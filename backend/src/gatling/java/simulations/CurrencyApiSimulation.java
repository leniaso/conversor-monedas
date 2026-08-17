package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

// FeederBuilder viene incluido en io.gatling.javaapi.core.*

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Performance test para el backend "conversor-monedas".
 *
 * Cubre los 5 endpoints de CurrencyController:
 *   GET  /api/currencies
 *   GET  /api/rates?base=...
 *   POST /api/convert
 *   GET  /api/history?page=...&size=...
 *   GET  /api/variation?from=...&to=...&start=...&end=...
 *
 * IMPORTANTE: /currencies, /rates, /convert y /variation dependen de la
 * API externa Frankfurter. Bajo carga alta, la latencia/errores que veas
 * ahí pueden venir de esa dependencia externa, no de tu código. /history
 * es el único endpoint 100% local (solo tu base de datos), útil como
 * referencia de "piso" de performance de tu propio stack.
 */
public class CurrencyApiSimulation extends Simulation {

    // ---------------------------------------------------------
    // Config: se puede sobreescribir con -DbaseUrl=... -Dusers=... etc.
    //   ./gradlew gatlingRun -DbaseUrl=http://localhost:8080 -Dusers=20
    // ---------------------------------------------------------
    private static final String BASE_URL =
            System.getProperty("baseUrl", "http://localhost:8080");
    private static final int USERS =
            Integer.parseInt(System.getProperty("users", "10"));
    private static final int RAMP_DURATION_SEC =
            Integer.parseInt(System.getProperty("rampSeconds", "20"));
    private static final int HOLD_DURATION_SEC =
            Integer.parseInt(System.getProperty("holdSeconds", "60"));

    HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling-PerformanceTest");

    // ---------------------------------------------------------
    // Datos de prueba (feeder): pares de monedas variados para no
    // pegarle siempre a la misma combinación (evita cacheo artificial
    // y simula tráfico real más heterogéneo).
    //
    // IMPORTANTE: Frankfurter obtiene sus tasas del Banco Central
    // Europeo (ECB), que solo publica ~30 monedas (principalmente
    // europeas + las más importantes a nivel global). NO incluye
    // COP (peso colombiano) ni muchas otras monedas latinoamericanas.
    // Usar una moneda no soportada hace que Frankfurter responda 404,
    // lo cual tu backend propaga como 500 (correctamente). Por eso
    // el feeder solo usa monedas que Frankfurter sí soporta.
    // ---------------------------------------------------------
    private static final FeederBuilder<Object> currencyPairsFeeder =
            listFeeder(List.of(
                    Map.of("from", "USD", "to", "EUR", "amount", 100),
                    Map.of("from", "EUR", "to", "USD", "amount", 250),
                    Map.of("from", "USD", "to", "GBP", "amount", 500),
                    Map.of("from", "GBP", "to", "USD", "amount", 75),
                    Map.of("from", "EUR", "to", "CHF", "amount", 300),
                    Map.of("from", "USD", "to", "JPY", "amount", 300)
            )).circular();

    // ---------------------------------------------------------
    // Escenario 1: GET /api/currencies  (catálogo, se espera muy rápido)
    // ---------------------------------------------------------
    ChainBuilder getCurrencies =
            exec(
                    http("GET /api/currencies")
                            .get("/api/currencies")
                            .check(status().is(200))
            );

    // ---------------------------------------------------------
    // Escenario 2: GET /api/rates?base=XXX
    // ---------------------------------------------------------
    ChainBuilder getRates =
            exec(
                    http("GET /api/rates")
                            .get("/api/rates")
                            .queryParam("base", "USD")
                            .check(status().is(200))
            );

    // ---------------------------------------------------------
    // Escenario 3: POST /api/convert
    // ---------------------------------------------------------
    ChainBuilder convert =
            feed(currencyPairsFeeder)
                    .exec(
                            http("POST /api/convert")
                                    .post("/api/convert")
                                    .body(StringBody(
                                            "{ \"from\": \"#{from}\", \"to\": \"#{to}\", \"amount\": #{amount} }"
                                    ))
                                    .check(status().is(200))
                    );

    // ---------------------------------------------------------
    // Escenario 4: GET /api/history?page=0&size=10  (único endpoint local)
    // ---------------------------------------------------------
    ChainBuilder getHistory =
            exec(
                    http("GET /api/history")
                            .get("/api/history")
                            .queryParam("page", "0")
                            .queryParam("size", "10")
                            .check(status().is(200))
            );

    // ---------------------------------------------------------
    // Escenario 5: GET /api/variation?from=&to=&start=&end=
    // ---------------------------------------------------------
    ChainBuilder getVariation =
            exec(
                    http("GET /api/variation")
                            .get("/api/variation")
                            .queryParam("from", "USD")
                            .queryParam("to", "EUR")
                            .queryParam("start", LocalDate.now().minusDays(30).toString())
                            .queryParam("end", LocalDate.now().toString())
                            .check(status().is(200))
            );

    // ---------------------------------------------------------
    // Escenarios independientes: cada uno pega SOLO a su endpoint,
    // en un loop, con una pausa corta entre repeticiones. Así cada
    // uno se puede leer de forma aislada en el reporte HTML
    // (pestaña "Scenarios" separa las métricas por escenario) y
    // queda claro cuál endpoint degrada primero.
    // ---------------------------------------------------------
    ScenarioBuilder scnCurrencies = scenario("GET /api/currencies")
            .during(Duration.ofSeconds(HOLD_DURATION_SEC)).on(
                    exec(getCurrencies)
                            .pause(Duration.ofMillis(200), Duration.ofMillis(500))
            );

    ScenarioBuilder scnRates = scenario("GET /api/rates")
            .during(Duration.ofSeconds(HOLD_DURATION_SEC)).on(
                    exec(getRates)
                            .pause(Duration.ofMillis(200), Duration.ofMillis(500))
            );

    ScenarioBuilder scnConvert = scenario("POST /api/convert")
            .during(Duration.ofSeconds(HOLD_DURATION_SEC)).on(
                    exec(convert)
                            .pause(Duration.ofMillis(200), Duration.ofMillis(500))
            );

    ScenarioBuilder scnHistory = scenario("GET /api/history")
            .during(Duration.ofSeconds(HOLD_DURATION_SEC)).on(
                    exec(getHistory)
                            .pause(Duration.ofMillis(200), Duration.ofMillis(500))
            );

    ScenarioBuilder scnVariation = scenario("GET /api/variation")
            .during(Duration.ofSeconds(HOLD_DURATION_SEC)).on(
                    exec(getVariation)
                            .pause(Duration.ofMillis(200), Duration.ofMillis(500))
            );

    {
        setUp(
                // Los 5 escenarios corren en paralelo, cada uno con su propia
                // rampa de usuarios. Si prefieres correrlos uno a la vez
                // (para que no compitan por recursos entre sí), comenta 4 de
                // los 5 bloques .injectOpen(...) antes de ejecutar, o crea
                // una clase Simulation por endpoint.
                scnCurrencies.injectOpen(
                        rampUsers(USERS).during(Duration.ofSeconds(RAMP_DURATION_SEC))
                ),
                scnRates.injectOpen(
                        rampUsers(USERS).during(Duration.ofSeconds(RAMP_DURATION_SEC))
                ),
                scnConvert.injectOpen(
                        rampUsers(USERS).during(Duration.ofSeconds(RAMP_DURATION_SEC))
                ),
                scnHistory.injectOpen(
                        rampUsers(USERS).during(Duration.ofSeconds(RAMP_DURATION_SEC))
                ),
                scnVariation.injectOpen(
                        rampUsers(USERS).during(Duration.ofSeconds(RAMP_DURATION_SEC))
                )
        )
                .protocols(httpProtocol)
                .assertions(
                        // Umbrales por escenario - ajústalos a tus SLAs reales.
                        // details(...) referencia el nombre del escenario/request.
                        forAll().responseTime().max().lt(3000),
                        forAll().successfulRequests().percent().gt(95.0),

                        // Endpoint 100% local: debería ser el más rápido y estable.
                        // Umbral ajustado tras la primera corrida real (p95 observado
                        // ~368ms contra una DB remota en Neon); 500ms deja margen
                        // razonable sin ser laxo.
                        details("GET /api/history").responseTime().percentile3().lt(500),

                        // Endpoints que dependen de Frankfurter: umbral más laxo
                        details("GET /api/currencies").responseTime().percentile3().lt(1000),
                        details("GET /api/rates").responseTime().percentile3().lt(1000),
                        details("POST /api/convert").responseTime().percentile3().lt(1200),
                        details("GET /api/variation").responseTime().percentile3().lt(1500)
                );
    }
}