package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.UsuarioUseCase;
import com.indux.induxbackend.application.port.in.UsuarioUseCase.ActualizarUsuarioComando;
import com.indux.induxbackend.application.port.in.UsuarioUseCase.RegistrarUsuarioComando;
import com.indux.induxbackend.domain.model.Usuario;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ActualizarUsuarioRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.RegistrarUsuarioRequest;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.UsuarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioUseCase usuarios;

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarios.listar().stream().map(UsuarioResponse::desde).toList();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistrarUsuarioRequest r) {
        Usuario creado = usuarios.registrar(
                new RegistrarUsuarioComando(r.empresaId(), r.nombre(), r.email(), r.password(), r.rol()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(creado));
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ActualizarUsuarioRequest r) {
        return UsuarioResponse.desde(usuarios.actualizar(id,
                new ActualizarUsuarioComando(r.empresaId(), r.nombre(), r.email(), r.password(), r.rol())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        usuarios.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}