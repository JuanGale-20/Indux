package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.ProcesoUseCase;
import com.indux.induxbackend.application.port.in.ProcesoUseCase.ActualizarProcesoComando;
import com.indux.induxbackend.application.port.in.ProcesoUseCase.CrearProcesoComando;
import com.indux.induxbackend.domain.model.Proceso;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ActualizarProcesoRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.CrearProcesoRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ProcesoResponse;
import com.indux.induxbackend.infrastructure.security.AccesoPorEmpresa;
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
public class ProcesoController {

    private final ProcesoUseCase procesos;
    private final AccesoPorEmpresa acceso;

    @PostMapping("/procesos")
    public ResponseEntity<ProcesoResponse> crear(@Valid @RequestBody CrearProcesoRequest r) {
        Proceso creado = procesos.crear(new CrearProcesoComando(
                r.productoId(), r.liderId(), r.codigo(), r.nombre(), r.secuencia(), r.unidadProcesada()));
        return ResponseEntity.created(URI.create("/api/procesos/" + creado.id()))
                .body(ProcesoResponse.desde(creado));
    }

    @GetMapping("/procesos/{id}")
    public ProcesoResponse obtener(@PathVariable Long id, Authentication autenticacion) {
        Proceso proceso = procesos.obtener(id);
        acceso.producto(autenticacion, proceso.productoId());
        return ProcesoResponse.desde(proceso);
    }

    @GetMapping("/productos/{productoId}/procesos")
    public List<ProcesoResponse> listarPorProducto(@PathVariable Long productoId,
                                                   Authentication autenticacion) {
        acceso.producto(autenticacion, productoId);
        return procesos.listarPorProducto(productoId).stream().map(ProcesoResponse::desde).toList();
    }

    @PutMapping("/procesos/{id}")
    public ProcesoResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ActualizarProcesoRequest r) {
        return ProcesoResponse.desde(procesos.actualizar(id, new ActualizarProcesoComando(
                r.liderId(), r.codigo(), r.nombre(), r.secuencia(), r.unidadProcesada())));
    }

    @DeleteMapping("/procesos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        procesos.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}