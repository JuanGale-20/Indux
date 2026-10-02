package com.indux.induxbackend.infrastructure.adapter.out.persistence.entity;

import com.indux.induxbackend.domain.model.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long empresaId;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}