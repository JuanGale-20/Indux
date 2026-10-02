package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.ProcesoUseCase;
import com.indux.induxbackend.application.port.in.ProcesoUseCase.CrearProcesoComando;
import com.indux.induxbackend.domain.model.Proceso;
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

    @GetMapping("/productos/{productoId}/procesos")
    public List<ProcesoResponse> listarPorProducto(@PathVariable Long productoId,
                                                   Authentication autenticacion) {
        acceso.producto(autenticacion, productoId);
        return procesos.listarPorProducto(productoId).stream().map(ProcesoResponse::desde).toList();
    }
}