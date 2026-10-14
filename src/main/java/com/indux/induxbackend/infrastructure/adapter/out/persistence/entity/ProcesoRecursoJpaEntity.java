package com.indux.induxbackend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "proceso_recurso")
@IdClass(ProcesoRecursoJpaEntity.Clave.class)
@Getter
@Setter
@NoArgsConstructor
public class ProcesoRecursoJpaEntity {

    @Id
    @Column(nullable = false)
    private Long procesoId;

    @Id
    @Column(nullable = false)
    private Long recursoId;

    @Column(precision = 12, scale = 6)
    private BigDecimal tiempoReferencia;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Clave implements Serializable {
        private Long procesoId;
        private Long recursoId;
    }
}