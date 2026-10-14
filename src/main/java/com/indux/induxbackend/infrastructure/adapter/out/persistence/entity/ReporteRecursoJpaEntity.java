package com.indux.induxbackend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "reporte_recurso")
@Getter
@Setter
@NoArgsConstructor
public class ReporteRecursoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporteId;

    @Column(nullable = false)
    private Long recursoId;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal tiempoProcesamiento;

    @Column(nullable = false, length = 20)
    private String unidadReferencia;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal tiempoCalendario;

    @Column(precision = 12, scale = 4)
    private BigDecimal tiempoNoDisponible;

    @Column(nullable = false)
    private Integer numeroTurnos;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal horasPorTurno;

    @Column(nullable = false)
    private Integer diasOperacion;
}