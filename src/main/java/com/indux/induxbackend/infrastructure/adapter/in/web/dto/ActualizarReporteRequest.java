package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.FrecuenciaReporte;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ActualizarReporteRequest(
        @NotBlank @Size(max = 120) String plantaArea,
        @NotNull FrecuenciaReporte frecuencia,
        @NotNull LocalDate periodoInicio,
        @NotNull LocalDate periodoFin,
        @NotNull @DecimalMin("0") BigDecimal demanda,
        @NotNull @DecimalMin("0") BigDecimal produccionReal,
        @Valid List<LineaReporteRequest> lineas) {
}
