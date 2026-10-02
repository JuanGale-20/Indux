package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.AutenticacionUseCase.LoginComando;
import com.indux.induxbackend.application.port.in.AutenticacionUseCase.SesionIniciada;
import com.indux.induxbackend.application.port.out.PasswordHasherPort;
import com.indux.induxbackend.application.port.out.TokenProviderPort;
import com.indux.induxbackend.application.port.out.UsuarioRepositoryPort;
import com.indux.induxbackend.domain.exception.CredencialesInvalidasException;
import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AutenticacionServiceTest {

    private final UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
    private final PasswordHasherPort hasher = mock(PasswordHasherPort.class);
    private final TokenProviderPort tokens = mock(TokenProviderPort.class);
    private final AutenticacionService service = new AutenticacionService(usuarios, hasher, tokens);

    private Usuario usuario(boolean activo) {
        return new Usuario(7L, 1L, "Ana", "ana@aceros.com", "HASH", Rol.LIDER, activo);
    }

    @Test
    void iniciaSesionConCredencialesCorrectas() {
        when(usuarios.buscarPorEmail("ana@aceros.com")).thenReturn(Optional.of(usuario(true)));
        when(hasher.coincide("secreto123", "HASH")).thenReturn(true);
        when(tokens.generar(any())).thenReturn("TOKEN");
        when(tokens.segundosDeVigencia()).thenReturn(7200L);

        SesionIniciada sesion = service.iniciarSesion(new LoginComando(" Ana@Aceros.com ", "secreto123"));

        assertThat(sesion.token()).isEqualTo("TOKEN");
        assertThat(sesion.expiraEnSegundos()).isEqualTo(7200L);
    }

    @Test
    void rechazaUnaContrasenaIncorrecta() {
        when(usuarios.buscarPorEmail("ana@aceros.com")).thenReturn(Optional.of(usuario(true)));
        when(hasher.coincide("mala", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> service.iniciarSesion(new LoginComando("ana@aceros.com", "mala")))
                .isInstanceOf(CredencialesInvalidasException.class);
        verify(tokens, never()).generar(any());
    }

    @Test
    void rechazaUnCorreoInexistente() {
        when(usuarios.buscarPorEmail("nadie@aceros.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.iniciarSesion(new LoginComando("nadie@aceros.com", "secreto123")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void rechazaUnUsuarioInactivo() {
        when(usuarios.buscarPorEmail("ana@aceros.com")).thenReturn(Optional.of(usuario(false)));
        when(hasher.coincide(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.iniciarSesion(new LoginComando("ana@aceros.com", "secreto123")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }
}