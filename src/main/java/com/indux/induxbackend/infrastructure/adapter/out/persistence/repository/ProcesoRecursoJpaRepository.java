package com.indux.induxbackend.infrastructure.adapter.out.persistence.repository;

import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ProcesoRecursoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcesoRecursoJpaRepository
        extends JpaRepository<ProcesoRecursoJpaEntity, ProcesoRecursoJpaEntity.Clave> {

    List<ProcesoRecursoJpaEntity> findByProcesoId(Long procesoId);

    List<ProcesoRecursoJpaEntity> findByRecursoId(Long recursoId);
}