package com.indux.induxbackend.infrastructure.adapter.out.persistence.adapter;

import com.indux.induxbackend.application.port.out.ValidacionRepositoryPort;
import com.indux.induxbackend.domain.model.ValidacionResultado;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper.ValidacionResultadoMapper;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.repository.ValidacionResultadoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ValidacionPersistenceAdapter implements ValidacionRepositoryPort {

    private final ValidacionResultadoJpaRepository repository;

    @Override
    @Transactional
    public void reemplazarPorReporte(Long reporteId, List<ValidacionResultado> resultados) {
        repository.deleteByReporteId(reporteId);
        repository.flush();
        if (resultados != null && !resultados.isEmpty()) {
            repository.saveAll(resultados.stream()
                    .map(r -> ValidacionResultadoMapper.toEntity(r.conReporte(reporteId)))
                    .toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValidacionResultado> listarPorReporte(Long reporteId) {
        return repository.findByReporteIdOrderByIdAsc(reporteId).stream()
                .map(ValidacionResultadoMapper::toDomain)
                .toList();
    }
}
