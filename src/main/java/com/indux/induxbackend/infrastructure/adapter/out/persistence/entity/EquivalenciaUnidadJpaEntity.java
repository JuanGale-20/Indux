package com.indux.induxbackend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "equivalencia_unidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquivalenciaUnidadJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long empresaId;

    private Long productoId;

    @Column(nullable = false, length = 20)
    private String unidadOrigen;

    @Column(nullable = false, length = 20)
    private String unidadDestino;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal factor;
}
