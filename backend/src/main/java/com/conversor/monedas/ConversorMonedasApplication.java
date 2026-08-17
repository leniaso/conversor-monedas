package com.conversor.monedas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
public class ConversorMonedasApplication {

    public static void main(String[] args) {
        loadDotEnvIntoSystemProperties();
        SpringApplication.run(ConversorMonedasApplication.class, args);
    }

    /**
     * Carga el archivo .env (si existe en la raíz del backend) como System properties,
     * para no depender de que el usuario exporte variables de entorno manualmente.
     * Las variables de entorno reales del sistema siguen teniendo prioridad si ya existen.
     */
    private static void loadDotEnvIntoSystemProperties() {
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile)) {
            System.out.println("[.env] No se encontró archivo .env en: " + envFile.toAbsolutePath());
            System.out.println("[.env] Directorio de trabajo actual: " + Path.of("").toAbsolutePath());
            return;
        }
        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int idx = trimmed.indexOf('=');
                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();

                // Quita comillas si las tiene
                if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                // Si ya viene de una variable de entorno real del sistema, no la pisamos
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
            System.out.println("[.env] Variables cargadas desde " + envFile.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[.env] No se pudo leer el archivo .env: " + e.getMessage());
        }
    }
}