package com.indux.induxbackend.infrastructure.adapter.in.web.dto;

import com.indux.induxbackend.domain.model.Empresa;

public record EmpresaResponse(Long id, String nombre, String sector, boolean activa) {

    public static EmpresaResponse desde(Empresa e) {
        return new EmpresaResponse(e.id(), e.nombre(), e.sector(), e.activa());
    }
}
