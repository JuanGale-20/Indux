package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.Empresa;
import java.util.List;

public interface EmpresaUseCase {

    Empresa crear(CrearEmpresaComando comando);

    Empresa obtener(Long id);

    List<Empresa> listar();

    record CrearEmpresaComando(String nombre, String sector) {
    }
}
