package com.indux.induxbackend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "validacion_resultado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionResultadoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporteId;

    @Column(nullable = false, length = 30)
    private String grupo;

    @Column(nullable = false, length = 60)
    private String variable;

    @Column(nullable = false, length = 20)
    private String severidad;

    @Column(nullable = false, length = 300)
    private String mensaje;
}
