package com.indux.induxbackend.infrastructure.adapter.out.persistence.adapter;

import com.indux.induxbackend.application.port.out.ProcesoRecursoRepositoryPort;
import com.indux.induxbackend.domain.model.ProcesoRecurso;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ProcesoRecursoJpaEntity;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper.ProcesoRecursoMapper;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.repository.ProcesoRecursoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProcesoRecursoPersistenceAdapter implements ProcesoRecursoRepositoryPort {

    private final ProcesoRecursoJpaRepository repository;

    @Override
    public ProcesoRecurso vincular(ProcesoRecurso vinculo) {
        return ProcesoRecursoMapper.toDomain(repository.save(ProcesoRecursoMapper.toEntity(vinculo)));
    }

    @Override
    public void desvincular(Long procesoId, Long recursoId) {
        repository.deleteById(new ProcesoRecursoJpaEntity.Clave(procesoId, recursoId));
    }

    @Override
    public boolean existe(Long procesoId, Long recursoId) {
        return repository.existsById(new ProcesoRecursoJpaEntity.Clave(procesoId, recursoId));
    }

    @Override
    public List<ProcesoRecurso> listarPorProceso(Long procesoId) {
        return repository.findByProcesoId(procesoId).stream().map(ProcesoRecursoMapper::toDomain).toList();
    }

    @Override
    public List<ProcesoRecurso> listarPorRecurso(Long recursoId) {
        return repository.findByRecursoId(recursoId).stream().map(ProcesoRecursoMapper::toDomain).toList();
    }
}