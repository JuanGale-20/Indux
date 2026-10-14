package com.indux.induxbackend.infrastructure.adapter.out.persistence.adapter;

import com.indux.induxbackend.application.port.out.ReporteRecursoRepositoryPort;
import com.indux.induxbackend.domain.model.ReporteRecurso;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper.ReporteRecursoMapper;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.repository.ReporteRecursoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReporteRecursoPersistenceAdapter implements ReporteRecursoRepositoryPort {

    private final ReporteRecursoJpaRepository repository;

    @Override
    public ReporteRecurso guardar(ReporteRecurso reporteRecurso) {
        return ReporteRecursoMapper.toDomain(repository.save(ReporteRecursoMapper.toEntity(reporteRecurso)));
    }

    @Override
    public List<ReporteRecurso> listarPorReporte(Long reporteId) {
        return repository.findByReporteId(reporteId).stream().map(ReporteRecursoMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void eliminarPorReporte(Long reporteId) {
        repository.deleteByReporteId(reporteId);
    }
}