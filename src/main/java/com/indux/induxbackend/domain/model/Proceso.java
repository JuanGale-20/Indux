package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;

import java.math.BigDecimal;

public record Proceso(Long id, Long productoId, Long liderId, String codigo,
                      String nombre, int secuencia, String unidadProcesada,
                      BigDecimal merma, String unidadEntrada) {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public Proceso {
        Reglas.idObligatorio(productoId, "El producto del proceso");
        codigo = Reglas.textoObligatorio(codigo, "El código del proceso");
        nombre = Reglas.textoObligatorio(nombre, "El nombre del proceso");
        unidadProcesada = Reglas.textoObligatorio(unidadProcesada, "La unidad procesada");
        if (secuencia <= 0) {
            throw new DominioException("La secuencia del proceso debe ser mayor que 0");
        }
        merma = (merma == null) ? BigDecimal.ZERO : merma;
        if (merma.signum() < 0 || merma.compareTo(CIEN) >= 0) {
            throw new DominioException("La merma debe estar entre 0 y menos de 100 por ciento");
        }
        unidadEntrada = (unidadEntrada == null || unidadEntrada.isBlank()) ? null : unidadEntrada.trim();
    }

    /** Constructor compatible: sin merma y con la unidad de entrada igual a la procesada. */
    public Proceso(Long id, Long productoId, Long liderId, String codigo,
                   String nombre, int secuencia, String unidadProcesada) {
        this(id, productoId, liderId, codigo, nombre, secuencia, unidadProcesada, BigDecimal.ZERO, null);
    }
}