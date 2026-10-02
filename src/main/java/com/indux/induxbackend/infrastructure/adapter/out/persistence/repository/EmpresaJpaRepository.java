package com.indux.induxbackend.infrastructure.adapter.out.persistence.repository;

import com.indux.induxbackend.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaJpaRepository extends JpaRepository<EmpresaJpaEntity, Long> {
}
