package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.Empresa;

public interface EmpresaUseCase {

    Empresa crear(CrearEmpresaComando comando);

    Empresa obtener(Long id);

    record CrearEmpresaComando(String nombre, String sector) {
    }
}
