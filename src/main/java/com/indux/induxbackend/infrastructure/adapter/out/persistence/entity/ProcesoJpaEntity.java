package com.indux.induxbackend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "proceso")
@Getter
@Setter
@NoArgsConstructor
public class ProcesoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productoId;

    private Long liderId;

    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private int secuencia;

    @Column(nullable = false, length = 20)
    private String unidadProcesada;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal merma = BigDecimal.ZERO;

    @Column(length = 20)
    private String unidadEntrada;
}