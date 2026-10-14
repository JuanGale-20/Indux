package com.indux.induxbackend.infrastructure.adapter.out.persistence.repository;

import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ReporteRecursoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReporteRecursoJpaRepository extends JpaRepository<ReporteRecursoJpaEntity, Long> {

    List<ReporteRecursoJpaEntity> findByReporteId(Long reporteId);

    void deleteByReporteId(Long reporteId);
}