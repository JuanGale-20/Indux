package com.indux.induxbackend.infrastructure.adapter.out.persistence.repository;

import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.AnalisisRecursoJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnalisisRecursoJpaRepository extends JpaRepository<AnalisisRecursoJpaEntity, Long> {

    @Query("""
        SELECT a, rr.recursoId, r.codigo, r.nombre
        FROM AnalisisRecursoJpaEntity a
        JOIN ReporteRecursoJpaEntity rr ON a.reporteRecursoId = rr.id
        JOIN RecursoJpaEntity r ON rr.recursoId = r.id
        WHERE rr.reporteId = :reporteId
        ORDER BY a.prioridad ASC NULLS LAST, a.utilizacion DESC
    """)
    List<Object[]> findDetallePorReporteId(@Param("reporteId") Long reporteId);

    @Query("""
        SELECT a, rr.recursoId, r.codigo, r.nombre,
               rep.id, rep.productoId, rep.periodoInicio, rep.periodoFin, rep.frecuencia
        FROM AnalisisRecursoJpaEntity a
        JOIN ReporteRecursoJpaEntity rr ON a.reporteRecursoId = rr.id
        JOIN RecursoJpaEntity r ON rr.recursoId = r.id
        JOIN ReporteJpaEntity rep ON rr.reporteId = rep.id
        WHERE rr.recursoId = :recursoId AND rep.estado = 'ANALIZADO'
        ORDER BY rep.periodoFin DESC, rep.id DESC
    """)
    List<Object[]> findHistoricoPorRecursoId(@Param("recursoId") Long recursoId, Pageable pageable);

    @Modifying
    @Query("""
        DELETE FROM AnalisisRecursoJpaEntity a
        WHERE a.reporteRecursoId IN (SELECT rr.id FROM ReporteRecursoJpaEntity rr WHERE rr.reporteId = :reporteId)
    """)
    void deleteByReporteId(@Param("reporteId") Long reporteId);
}
