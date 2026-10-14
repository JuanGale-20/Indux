package com.indux.induxbackend.infrastructure.adapter.out.persistence.entity;

import com.indux.induxbackend.domain.model.EstadoReporte;
import com.indux.induxbackend.domain.model.FrecuenciaReporte;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reporte")
@Getter
@Setter
@NoArgsConstructor
public class ReporteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private Long creadoPor;

    @Column(nullable = false, length = 120)
    private String plantaArea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FrecuenciaReporte frecuencia;

    @Column(nullable = false)
    private LocalDate periodoInicio;

    @Column(nullable = false)
    private LocalDate periodoFin;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal demanda;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal produccionReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReporte estado;
}