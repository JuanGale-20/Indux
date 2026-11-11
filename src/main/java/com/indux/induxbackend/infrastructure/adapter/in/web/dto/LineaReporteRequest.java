package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LineaReporteRequest(
        @NotNull Long recursoId,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal tiempoProcesamiento,
        @NotBlank String unidadReferencia,
        @NotNull @DecimalMin("0") BigDecimal tiempoCalendario,
        @DecimalMin("0") BigDecimal tiempoNoDisponible,
        @NotNull @Min(1) Integer numeroTurnos,
        @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("24") BigDecimal horasPorTurno,
        @NotNull @Min(1) Integer diasOperacion) {
}
