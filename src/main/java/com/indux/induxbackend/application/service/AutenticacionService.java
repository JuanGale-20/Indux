package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.AutenticacionUseCase;
import com.indux.induxbackend.application.port.out.PasswordHasherPort;
import com.indux.induxbackend.application.port.out.TokenProviderPort;
import com.indux.induxbackend.application.port.out.UsuarioRepositoryPort;
import com.indux.induxbackend.domain.exception.CredencialesInvalidasException;
import com.indux.induxbackend.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutenticacionService implements AutenticacionUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final PasswordHasherPort hasher;
    private final TokenProviderPort tokens;

    @Override
    public SesionIniciada iniciarSesion(LoginComando c) {
        String email = c.email() == null ? "" : c.email().trim().toLowerCase();

        Usuario usuario = usuarios.buscarPorEmail(email).orElse(null);
        boolean valido = usuario != null
                && usuario.activo()
                && c.password() != null
                && hasher.coincide(c.password(), usuario.passwordHash());

        // Mismo error para correo inexistente, clave incorrecta o usuario inactivo:
        // así no se revela qué correos existen
        if (!valido) {
            throw new CredencialesInvalidasException();
        }
        return new SesionIniciada(tokens.generar(usuario), tokens.segundosDeVigencia(), usuario);
    }
}