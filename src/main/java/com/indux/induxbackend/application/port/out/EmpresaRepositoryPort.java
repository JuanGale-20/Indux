package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.Empresa;
import java.util.List;
import java.util.Optional;

public interface EmpresaRepositoryPort {
    Empresa guardar(Empresa empresa);
    Optional<Empresa> buscarPorId(Long id);
    List<Empresa> listar();
}
