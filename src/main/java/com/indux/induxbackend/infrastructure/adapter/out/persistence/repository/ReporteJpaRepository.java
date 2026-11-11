package com.indux.induxbackend.infrastructure.adapter.out.persistence.repository;

import com.indux.induxbackend.domain.model.FrecuenciaReporte;
import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ReporteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReporteJpaRepository extends JpaRepository<ReporteJpaEntity, Long> {

    List<ReporteJpaEntity> findByProductoIdOrderByPeriodoInicioDesc(Long productoId);

    @Query("select r from ReporteJpaEntity r where r.productoId in "
            + "(select p.id from ProductoJpaEntity p where p.empresaId = :empresaId) "
            + "order by r.periodoInicio desc")
    List<ReporteJpaEntity> findByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("""
        SELECT r FROM ReporteJpaEntity r
        JOIN ProductoJpaEntity p ON p.id = r.productoId
        WHERE p.empresaId = :empresaId
          AND r.productoId <> :productoIdExcluir
          AND r.estado IN ('VALIDADO', 'ANALIZADO')
          AND r.frecuencia = :frecuencia
          AND r.periodoInicio <= :fin
          AND r.periodoFin >= :inicio
        ORDER BY r.periodoFin DESC, r.id DESC
    """)
    List<ReporteJpaEntity> buscarCandidatosRecursoCompartido(
            @Param("empresaId") Long empresaId,
            @Param("productoIdExcluir") Long productoIdExcluir,
            @Param("frecuencia") FrecuenciaReporte frecuencia,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);
}
