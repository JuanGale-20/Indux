package com.indux.induxbackend.infrastructure.adapter.out.persistence.repository;

import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ReporteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReporteJpaRepository extends JpaRepository<ReporteJpaEntity, Long> {

    List<ReporteJpaEntity> findByProductoIdOrderByPeriodoInicioDesc(Long productoId);

    @Query("select r from ReporteJpaEntity r where r.productoId in "
            + "(select p.id from ProductoJpaEntity p where p.empresaId = :empresaId) "
            + "order by r.periodoInicio desc")
    List<ReporteJpaEntity> findByEmpresaId(@Param("empresaId") Long empresaId);
}