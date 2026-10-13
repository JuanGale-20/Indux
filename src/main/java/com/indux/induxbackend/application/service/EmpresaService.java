package com.indux.induxbackend.application.service;


import com.indux.induxbackend.application.port.in.EmpresaUseCase;
import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.Empresa;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpresaService implements EmpresaUseCase {

    private final EmpresaRepositoryPort empresas;

    @Override
    public Empresa crear(CrearEmpresaComando comando) {
        return empresas.guardar(new Empresa(null, comando.nombre(), comando.sector(), true));
    }

    @Override
    @Transactional(readOnly = true)
    public Empresa obtener(Long id) {
        return empresas.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("No existe la empresa con id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empresa> listar() {
        return empresas.listar();
    }
}
