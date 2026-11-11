package com.indux.induxbackend.infrastructure.adapter.out.persistence.repository;

import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.EquivalenciaUnidadJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquivalenciaUnidadJpaRepository extends JpaRepository<EquivalenciaUnidadJpaEntity, Long> {

    @Query("""
        SELECT e FROM EquivalenciaUnidadJpaEntity e
        WHERE (e.empresaId IS NULL AND e.productoId IS NULL)
           OR (:empresaId IS NOT NULL AND e.empresaId = :empresaId AND e.productoId IS NULL)
           OR (:productoId IS NOT NULL AND e.productoId = :productoId)
    """)
    List<EquivalenciaUnidadJpaEntity> findByEmpresaYProducto(
            @Param("empresaId") Long empresaId,
            @Param("productoId") Long productoId);
}
