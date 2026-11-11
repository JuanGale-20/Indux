package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.FrecuenciaReporte;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class NormalizadorImportacion {

    private static final List<DateTimeFormatter> FORMATOS_FECHA = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,                      // yyyy-MM-dd
            DateTimeFormatter.ofPattern("d/M/yyyy"),              // dd/MM/yyyy o d/M/yyyy
            DateTimeFormatter.ofPattern("d-M-yyyy")               // dd-MM-yyyy o d-M-yyyy
    );

    private NormalizadorImportacion() {}

    public static String normalizarEncabezado(String encabezado) {
        if (encabezado == null) {
            return "";
        }
        String limpio = encabezado.trim().toLowerCase();
        // Quitar acentos
        limpio = Normalizer.normalize(limpio, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        // Espacios, guiones y puntos a guion bajo
        limpio = limpio.replaceAll("[\\s\\-\\.]+", "_");
        // Quitar caracteres no alfanuméricos ni guion bajo
        limpio = limpio.replaceAll("[^a-z0-9_]", "");
        return limpio;
    }

    public static BigDecimal normalizarNumero(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String s = texto.trim().replace(" ", "");

        int indexPunto = s.lastIndexOf('.');
        int indexComa = s.lastIndexOf(',');

        if (indexPunto != -1 && indexComa != -1) {
            if (indexComa > indexPunto) {
                // Formato europeo/latino: 1.234,56
                s = s.replace(".", "").replace(",", ".");
            } else {
                // Formato anglosajón: 1,234.56
                s = s.replace(",", "");
            }
        } else if (indexComa != -1) {
            // Solo coma: 1234,5
            s = s.replace(",", ".");
        }
        return new BigDecimal(s);
    }

    public static LocalDate parsearFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String s = texto.trim();
        for (DateTimeFormatter fmt : FORMATOS_FECHA) {
            try {
                return LocalDate.parse(s, fmt);
            } catch (DateTimeParseException ignored) {
                // Siguiente formato
            }
        }
        throw new IllegalArgumentException("Fecha inválida '" + texto + "'. Formatos aceptados: AAAA-MM-DD o DD/MM/AAAA");
    }

    public static FrecuenciaReporte parsearFrecuencia(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return FrecuenciaReporte.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Frecuencia inválida '" + texto + "'. Valores válidos: DIARIA, SEMANAL, MENSUAL, ANUAL");
        }
    }
}
