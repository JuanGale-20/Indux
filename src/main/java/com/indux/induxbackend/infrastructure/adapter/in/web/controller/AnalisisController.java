package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.AnalisisUseCase;
import com.indux.induxbackend.application.port.in.AnalisisUseCase.AnalisisComando;
import com.indux.induxbackend.application.port.in.AnalisisUseCase.RecursoComando;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.AnalisisResponse;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.AnalizarRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analisis")
@RequiredArgsConstructor
@Tag(name = "Análisis al vuelo", description = "Cálculo instantáneo de capacidad e indicadores sin persistencia")
public class AnalisisController {

    private final AnalisisUseCase analisis;

    @Operation(summary = "Calcular indicadores de cuello de botella al vuelo")
    @PostMapping("/calcular")
    public AnalisisResponse calcular(@Valid @RequestBody AnalizarRequest r) {
        var comando = new AnalisisComando(r.demanda(), r.recursos().stream()
                .map(x -> new RecursoComando(x.nombre(), x.tiempoProcesamiento(), x.turnos(),
                        x.horasPorTurno(), x.diasOperacion(), x.cantidadRecursos()))
                .toList());
        return AnalisisResponse.desde(analisis.analizar(comando));
    }
}
