package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record AnalizarRequest(
        @NotNull @PositiveOrZero BigDecimal demanda,
        @NotEmpty @Valid List<RecursoRequest> recursos) {

    public record RecursoRequest(
            @NotBlank String nombre,
            @NotNull @Positive BigDecimal tiempoProcesamiento,
            @Positive int turnos,
            @NotNull @Positive BigDecimal horasPorTurno,
            @Positive int diasOperacion,
            @Positive int cantidadRecursos) {
    }
}