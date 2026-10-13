package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.Empresa;

import java.util.List;

public interface EmpresaUseCase {

    Empresa crear(CrearEmpresaComando comando);

    Empresa obtener(Long id);

    List<Empresa> listar();

    Empresa actualizar(Long id, ActualizarEmpresaComando comando);

    void desactivar(Long id);

    record CrearEmpresaComando(String nombre, String sector) {
    }

    record ActualizarEmpresaComando(String nombre, String sector) {
    }
}