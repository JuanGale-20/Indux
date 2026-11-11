package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.ProcesoUseCase;
import com.indux.induxbackend.application.port.in.RecursoUseCase;
import com.indux.induxbackend.application.port.in.RecursoUseCase.ActualizarRecursoComando;
import com.indux.induxbackend.application.port.in.RecursoUseCase.CrearRecursoComando;
import com.indux.induxbackend.application.port.in.RecursoUseCase.VincularProcesoComando;
import com.indux.induxbackend.domain.model.EstadoRecurso;
import com.indux.induxbackend.domain.model.HistoricoRecurso;
import com.indux.induxbackend.domain.model.Proceso;
import com.indux.induxbackend.domain.model.ProcesoRecurso;
import com.indux.induxbackend.domain.model.Recurso;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.*;
import com.indux.induxbackend.infrastructure.security.AccesoPorEmpresa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Recursos", description = "Gestión de recursos (máquinas, líneas, estaciones) y sus vínculos")
public class RecursoController {

    private final RecursoUseCase recursos;
    private final ProcesoUseCase procesos;
    private final AccesoPorEmpresa acceso;

    @Operation(summary = "Crear un nuevo recurso (ADMIN)")
    @PostMapping("/recursos")
    public ResponseEntity<RecursoResponse> crear(@Valid @RequestBody CrearRecursoRequest r) {
        Recurso creado = recursos.crear(new CrearRecursoComando(
                r.empresaId(), r.codigo(), r.nombre(), r.tipo(), r.cantidadDisponible(),
                EstadoRecurso.desde(r.estado())));
        return ResponseEntity.created(URI.create("/api/recursos/" + creado.id()))
                .body(RecursoResponse.desde(creado));
    }

    @Operation(summary = "Listar recursos de una empresa")
    @GetMapping("/recursos")
    public List<RecursoResponse> listarPorEmpresa(@RequestParam Long empresaId,
                                                  Authentication autenticacion) {
        acceso.verificarEmpresa(autenticacion, empresaId);
        return recursos.listarPorEmpresa(empresaId).stream().map(RecursoResponse::desde).toList();
    }

    @Operation(summary = "Obtener un recurso por ID")
    @GetMapping("/recursos/{id}")
    public RecursoResponse obtener(@PathVariable Long id, Authentication autenticacion) {
        Recurso recurso = recursos.obtener(id);
        acceso.verificarEmpresa(autenticacion, recurso.empresaId());
        return RecursoResponse.desde(recurso);
    }

    @Operation(summary = "Actualizar datos de un recurso (ADMIN)")
    @PutMapping("/recursos/{id}")
    public RecursoResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ActualizarRecursoRequest r) {
        return RecursoResponse.desde(recursos.actualizar(id, new ActualizarRecursoComando(
                r.codigo(), r.nombre(), r.tipo(), r.cantidadDisponible(),
                EstadoRecurso.desde(r.estado()))));
    }

    @Operation(summary = "Eliminar un recurso (ADMIN)")
    @DeleteMapping("/recursos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        recursos.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar vínculos de un recurso con procesos")
    @GetMapping("/recursos/{id}/procesos")
    public List<ProcesoRecursoResponse> listarVinculos(@PathVariable Long id,
                                                       Authentication autenticacion) {
        acceso.verificarEmpresa(autenticacion, recursos.obtener(id).empresaId());
        return recursos.listarVinculos(id).stream().map(ProcesoRecursoResponse::desde).toList();
    }

    @Operation(summary = "Vincular un recurso a un proceso con tiempo de referencia (ADMIN)")
    @PostMapping("/recursos/{id}/procesos")
    public ResponseEntity<ProcesoRecursoResponse> vincular(@PathVariable Long id,
                                                           @Valid @RequestBody VincularProcesoRequest r) {
        ProcesoRecurso vinculo = recursos.vincular(id,
                new VincularProcesoComando(r.procesoId(), r.tiempoReferencia()));
        return ResponseEntity.created(URI.create("/api/recursos/" + id + "/procesos/" + vinculo.procesoId()))
                .body(ProcesoRecursoResponse.desde(vinculo));
    }

    @Operation(summary = "Desvincular un recurso de un proceso (ADMIN)")
    @DeleteMapping("/recursos/{id}/procesos/{procesoId}")
    public ResponseEntity<Void> desvincular(@PathVariable Long id, @PathVariable Long procesoId) {
        recursos.desvincular(id, procesoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar recursos vinculados a un proceso")
    @GetMapping("/procesos/{procesoId}/recursos")
    public List<RecursoResponse> listarPorProceso(@PathVariable Long procesoId,
                                                  Authentication autenticacion) {
        Proceso proceso = procesos.obtener(procesoId);
        acceso.producto(autenticacion, proceso.productoId());
        return recursos.listarPorProceso(procesoId).stream().map(RecursoResponse::desde).toList();
    }

    @Operation(summary = "Consultar histórico y tendencia de utilización de un recurso")
    @GetMapping("/recursos/{id}/historico")
    public HistoricoRecursoResponse obtenerHistorico(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "12") int limite,
                                                     Authentication autenticacion) {
        Recurso recurso = recursos.obtener(id);
        acceso.verificarEmpresa(autenticacion, recurso.empresaId());
        HistoricoRecurso historico = recursos.obtenerHistorico(id, limite);
        return HistoricoRecursoResponse.desde(historico);
    }
}
