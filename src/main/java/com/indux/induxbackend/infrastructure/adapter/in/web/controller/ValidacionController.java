package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.ValidacionUseCase;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ResultadoValidacionResponse;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ValidarEmpresaRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ValidarProcesoRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ValidarProductoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Valida datos según la Fase 2 de SteelForce sin guardar nada. */
@RestController
@RequestMapping("/api/validaciones")
@RequiredArgsConstructor
public class ValidacionController {

    private final ValidacionUseCase validacion;

    @PostMapping("/empresa")
    public ResultadoValidacionResponse empresa(@RequestBody ValidarEmpresaRequest r) {
        return ResultadoValidacionResponse.desde(validacion.validarEmpresa(r.nombre(), r.sector()));
    }

    @PostMapping("/producto")
    public ResultadoValidacionResponse producto(@RequestBody ValidarProductoRequest r) {
        return ResultadoValidacionResponse.desde(
                validacion.validarProducto(r.codigo(), r.nombre(), r.unidadMedida()));
    }

    @PostMapping("/proceso")
    public ResultadoValidacionResponse proceso(@RequestBody ValidarProcesoRequest r) {
        int secuencia = r.secuencia() == null ? 0 : r.secuencia();
        return ResultadoValidacionResponse.desde(
                validacion.validarProceso(r.codigo(), r.nombre(), secuencia, r.unidadProcesada()));
    }
}
