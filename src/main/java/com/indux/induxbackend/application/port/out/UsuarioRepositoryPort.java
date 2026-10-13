package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(String email);

    boolean existePorEmail(String email);

    List<Usuario> listar();
}