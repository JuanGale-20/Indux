package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.UsuarioUseCase;
import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.application.port.out.PasswordHasherPort;
import com.indux.induxbackend.application.port.out.UsuarioRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService implements UsuarioUseCase {

    private static final int LONGITUD_MINIMA_PASSWORD = 8;

    private final UsuarioRepositoryPort usuarios;
    private final EmpresaRepositoryPort empresas;
    private final PasswordHasherPort hasher;

    @Override
    public Usuario registrar(RegistrarUsuarioComando c) {
        if (c.password() == null || c.password().length() < LONGITUD_MINIMA_PASSWORD) {
            throw new DominioException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA_PASSWORD + " caracteres");
        }

        Usuario nuevo = new Usuario(null, c.empresaId(), c.nombre(), c.email(),
                hasher.hash(c.password()), c.rol(), true);

        if (nuevo.empresaId() != null) {
            empresas.buscarPorId(nuevo.empresaId())
                    .orElseThrow(() -> new NoEncontradoException(
                            "No existe la empresa con id " + nuevo.empresaId()));
        }
        if (usuarios.existePorEmail(nuevo.email())) {
            throw new ConflictoException("Ya existe un usuario con el correo " + nuevo.email());
        }
        return usuarios.guardar(nuevo);
    }
}