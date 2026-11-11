package com.indux.induxbackend.infrastructure.adapter.out.persistence.repository;

import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.ValidacionResultadoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValidacionResultadoJpaRepository extends JpaRepository<ValidacionResultadoJpaEntity, Long> {

    List<ValidacionResultadoJpaEntity> findByReporteIdOrderByIdAsc(Long reporteId);

    void deleteByReporteId(Long reporteId);
}
