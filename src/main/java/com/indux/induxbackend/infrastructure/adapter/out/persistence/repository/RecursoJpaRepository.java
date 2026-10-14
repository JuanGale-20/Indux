package com.indux.induxbackend.infrastructure.adapter.out.persistence.repository;

import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.RecursoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecursoJpaRepository extends JpaRepository<RecursoJpaEntity, Long> {

    List<RecursoJpaEntity> findByEmpresaId(Long empresaId);

    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);

    @Query("select r from RecursoJpaEntity r where r.id in "
            + "(select pr.recursoId from ProcesoRecursoJpaEntity pr where pr.procesoId = :procesoId)")
    List<RecursoJpaEntity> findByProcesoId(@Param("procesoId") Long procesoId);
}