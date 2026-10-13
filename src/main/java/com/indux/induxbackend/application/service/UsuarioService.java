package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.UsuarioUseCase;
import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.application.port.out.PasswordHasherPort;
import com.indux.induxbackend.application.port.out.UsuarioRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        validarPassword(c.password());

        Usuario nuevo = new Usuario(null, c.empresaId(), c.nombre(), c.email(),
                hasher.hash(c.password()), c.rol(), true);

        verificarEmpresa(nuevo.empresaId());
        if (usuarios.existePorEmail(nuevo.email())) {
            throw new ConflictoException("Ya existe un usuario con el correo " + nuevo.email());
        }
        return usuarios.guardar(nuevo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarios.listar();
    }

    @Override
    public Usuario actualizar(Long id, ActualizarUsuarioComando c) {
        Usuario actual = buscar(id);

        String hash = actual.passwordHash();
        if (c.password() != null && !c.password().isBlank()) {
            validarPassword(c.password());
            hash = hasher.hash(c.password());
        }

        Usuario nuevo = new Usuario(id, c.empresaId(), c.nombre(), c.email(), hash, c.rol(), actual.activo());

        verificarEmpresa(nuevo.empresaId());
        if (!nuevo.email().equals(actual.email()) && usuarios.existePorEmail(nuevo.email())) {
            throw new ConflictoException("Ya existe un usuario con el correo " + nuevo.email());
        }
        return usuarios.guardar(nuevo);
    }

    @Override
    public void desactivar(Long id) {
        Usuario actual = buscar(id);
        if (actual.rol() == Rol.ADMIN) {
            throw new ConflictoException("No se puede desactivar a un administrador de la plataforma");
        }
        usuarios.guardar(new Usuario(id, actual.empresaId(), actual.nombre(), actual.email(),
                actual.passwordHash(), actual.rol(), false));
    }

    private Usuario buscar(Long id) {
        return usuarios.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("No existe el usuario con id " + id));
    }

    private void verificarEmpresa(Long empresaId) {
        if (empresaId != null) {
            empresas.buscarPorId(empresaId)
                    .orElseThrow(() -> new NoEncontradoException("No existe la empresa con id " + empresaId));
        }
    }

    private void validarPassword(String password) {
        if (password == null || password.length() < LONGITUD_MINIMA_PASSWORD) {
            throw new DominioException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA_PASSWORD + " caracteres");
        }
    }
}