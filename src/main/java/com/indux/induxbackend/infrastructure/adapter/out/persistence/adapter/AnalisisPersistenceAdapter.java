package com.indux.induxbackend.infrastructure.adapter.out.persistence.adapter;

import com.indux.induxbackend.application.port.out.AnalisisRepositoryPort;
import com.indux.induxbackend.domain.model.AnalisisRecursoReporte;
import com.indux.induxbackend.domain.model.ClasificacionRestriccion;
import com.indux.induxbackend.domain.model.FrecuenciaReporte;
import com.indux.induxbackend.domain.model.PuntoHistoricoRecurso;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.AnalisisRecursoJpaEntity;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.mapper.AnalisisRecursoMapper;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.repository.AnalisisRecursoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalisisPersistenceAdapter implements AnalisisRepositoryPort {

    private final AnalisisRecursoJpaRepository repository;

    @Override
    @Transactional
    public void reemplazarPorReporte(Long reporteId, List<AnalisisRecursoReporte> analisis) {
        repository.deleteByReporteId(reporteId);
        repository.flush();
        if (analisis != null && !analisis.isEmpty()) {
            repository.saveAll(analisis.stream()
                    .map(AnalisisRecursoMapper::toEntity)
                    .toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalisisRecursoReporte> listarPorReporte(Long reporteId) {
        List<Object[]> filas = repository.findDetallePorReporteId(reporteId);
        List<AnalisisRecursoReporte> resultado = new ArrayList<>();
        for (Object[] f : filas) {
            AnalisisRecursoJpaEntity entity = (AnalisisRecursoJpaEntity) f[0];
            Long recursoId = (Long) f[1];
            String codigo = (String) f[2];
            String nombre = (String) f[3];
            resultado.add(AnalisisRecursoMapper.toDomain(entity, recursoId, codigo, nombre));
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalisisRecursoReporte> listarPorRecurso(Long recursoId, int limite) {
        List<Object[]> filas = repository.findHistoricoPorRecursoId(recursoId, PageRequest.of(0, limite));
        List<AnalisisRecursoReporte> resultado = new ArrayList<>();
        for (Object[] f : filas) {
            AnalisisRecursoJpaEntity entity = (AnalisisRecursoJpaEntity) f[0];
            Long recId = (Long) f[1];
            String codigo = (String) f[2];
            String nombre = (String) f[3];
            resultado.add(AnalisisRecursoMapper.toDomain(entity, recId, codigo, nombre));
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoHistoricoRecurso> listarHistoricoPorRecurso(Long recursoId, int limite) {
        List<Object[]> filas = repository.findHistoricoPorRecursoId(recursoId, PageRequest.of(0, limite));
        List<PuntoHistoricoRecurso> resultado = new ArrayList<>();
        for (Object[] f : filas) {
            AnalisisRecursoJpaEntity entity = (AnalisisRecursoJpaEntity) f[0];
            Long reporteId = (Long) f[4];
            Long productoId = (Long) f[5];
            LocalDate periodoInicio = (LocalDate) f[6];
            LocalDate periodoFin = (LocalDate) f[7];
            FrecuenciaReporte frecuencia = (FrecuenciaReporte) f[8];
            ClasificacionRestriccion clasificacion = entity.getClasificacion() != null
                    ? ClasificacionRestriccion.valueOf(entity.getClasificacion())
                    : null;

            resultado.add(new PuntoHistoricoRecurso(
                    reporteId,
                    productoId,
                    periodoInicio,
                    periodoFin,
                    frecuencia,
                    entity.getUtilizacion(),
                    entity.getHorasRequeridas(),
                    entity.getHorasDisponibles(),
                    entity.getDeficitHoras(),
                    entity.getImpacto(),
                    clasificacion
            ));
        }
        return resultado;
    }
}
