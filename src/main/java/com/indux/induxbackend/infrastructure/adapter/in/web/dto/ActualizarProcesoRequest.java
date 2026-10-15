package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ActualizarProcesoRequest(
        Long liderId,
        @NotBlank String codigo,
        @NotBlank String nombre,
        @NotNull @Positive Integer secuencia,
        @NotBlank String unidadProcesada,
        @DecimalMin("0") @DecimalMax("99.99") BigDecimal merma,
        String unidadEntrada) {
}