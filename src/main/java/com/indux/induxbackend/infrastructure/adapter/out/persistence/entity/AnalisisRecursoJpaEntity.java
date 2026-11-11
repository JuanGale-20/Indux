package com.indux.induxbackend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "analisis_recurso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalisisRecursoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long reporteRecursoId;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal horasRequeridas;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal horasDisponibles;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal capacidad;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal utilizacion;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal holgura;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal deficitCapacidad;

    @Column(nullable = false, length = 30)
    private String clasificacion;

    private Integer prioridad;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(nullable = false)
    private Instant calculadoEn;

    @Column(precision = 14, scale = 4)
    private BigDecimal aporteHoras;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal deficitHoras;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal impacto;
}
