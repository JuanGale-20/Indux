package com.indux.induxbackend.infrastructure.adapter.out.persistence.adapter;

import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper.EmpresaMapper;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.repository.EmpresaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmpresaPersistenceAdapter implements EmpresaRepositoryPort {

    private final EmpresaJpaRepository repository;

    @Override
    public Empresa guardar(Empresa empresa) {
        return EmpresaMapper.toDomain(repository.save(EmpresaMapper.toEntity(empresa)));
    }

    @Override
    public Optional<Empresa> buscarPorId(Long id) {
        return repository.findById(id).map(EmpresaMapper::toDomain);
    }
}
