package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.EmpresaUseCase;
import com.indux.induxbackend.application.port.in.EmpresaUseCase.ActualizarEmpresaComando;
import com.indux.induxbackend.application.port.in.EmpresaUseCase.CrearEmpresaComando;
import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ActualizarEmpresaRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.CrearEmpresaRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.EmpresaResponse;
import com.indux.induxbackend.infrastructure.security.AccesoPorEmpresa;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaUseCase empresas;
    private final AccesoPorEmpresa acceso;

    @PostMapping
    public ResponseEntity<EmpresaResponse> crear(@Valid @RequestBody CrearEmpresaRequest request) {
        Empresa creada = empresas.crear(new CrearEmpresaComando(request.nombre(), request.sector()));
        return ResponseEntity.created(URI.create("/api/empresas/" + creada.id()))
                .body(EmpresaResponse.desde(creada));
    }

    @GetMapping
    public List<EmpresaResponse> listar() {
        return empresas.listar().stream().map(EmpresaResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public EmpresaResponse obtener(@PathVariable Long id, Authentication autenticacion) {
        acceso.verificarEmpresa(autenticacion, id);
        return EmpresaResponse.desde(empresas.obtener(id));
    }

    @PutMapping("/{id}")
    public EmpresaResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ActualizarEmpresaRequest r) {
        return EmpresaResponse.desde(
                empresas.actualizar(id, new ActualizarEmpresaComando(r.nombre(), r.sector())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        empresas.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
