package com.indux.induxbackend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
public class EmpresaJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 80)
    private String sector;

    @Column(nullable = false)
    private boolean activa;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
