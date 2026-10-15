package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record VincularProcesoRequest(
        @NotNull Long procesoId,
        @Positive BigDecimal tiempoReferencia) {
}