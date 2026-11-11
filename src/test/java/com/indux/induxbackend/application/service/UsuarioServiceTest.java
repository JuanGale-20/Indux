package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.UsuarioUseCase.ActualizarUsuarioComando;
import com.indux.induxbackend.application.port.in.UsuarioUseCase.RegistrarUsuarioComando;
import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.application.port.out.PasswordHasherPort;
import com.indux.induxbackend.application.port.out.UsuarioRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private final UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
    private final EmpresaRepositoryPort empresas = mock(EmpresaRepositoryPort.class);
    private final PasswordHasherPort hasher = mock(PasswordHasherPort.class);
    private final UsuarioService service = new UsuarioService(usuarios, empresas, hasher);

    private RegistrarUsuarioComando lider(String password) {
        return new RegistrarUsuarioComando(1L, "Ana", " Ana@Aceros.com ", password, Rol.LIDER);
    }

    private void empresaExiste() {
        when(empresas.buscarPorId(1L)).thenReturn(Optional.of(new Empresa(1L, "Aceros", null, true)));
    }

    @Test
    void registraNormalizaElCorreoYGuardaSoloElHash() {
        empresaExiste();
        when(hasher.hash("secreto123")).thenReturn("HASH");
        when(usuarios.existePorEmail("ana@aceros.com")).thenReturn(false);
        when(usuarios.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario u = service.registrar(lider("secreto123"));

        assertThat(u.email()).isEqualTo("ana@aceros.com");
        assertThat(u.passwordHash()).isEqualTo("HASH");
        assertThat(u.activo()).isTrue();
    }

    @Test
    void rechazaUnaContrasenaCorta() {
        assertThatThrownBy(() -> service.registrar(lider("corta")))
                .isInstanceOf(DominioException.class)
                .hasMessageContaining("8 caracteres");
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void rechazaUnCorreoDuplicado() {
        empresaExiste();
        when(hasher.hash(any())).thenReturn("HASH");
        when(usuarios.existePorEmail("ana@aceros.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(lider("secreto123")))
                .isInstanceOf(ConflictoException.class);
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void rechazaUnaEmpresaInexistente() {
        when(empresas.buscarPorId(1L)).thenReturn(Optional.empty());
        when(hasher.hash(any())).thenReturn("HASH");

        assertThatThrownBy(() -> service.registrar(lider("secreto123")))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void elAdministradorSeRegistraSinEmpresa() {
        when(hasher.hash(any())).thenReturn("HASH");
        when(usuarios.existePorEmail("root@indux.com")).thenReturn(false);
        when(usuarios.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario u = service.registrar(
                new RegistrarUsuarioComando(null, "Root", "root@indux.com", "secreto123", Rol.ADMIN));

        assertThat(u.rol()).isEqualTo(Rol.ADMIN);
        assertThat(u.empresaId()).isNull();
        verifyNoInteractions(empresas);
    }

    @Test
    void actualizarCambiaNombreYRol() {
        empresaExiste();
        Usuario actual = new Usuario(10L, 1L, "Carlos", "carlos@aceros.com", "OLD_HASH", Rol.LIDER, true);
        when(usuarios.buscarPorId(10L)).thenReturn(Optional.of(actual));
        when(usuarios.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        ActualizarUsuarioComando cmd = new ActualizarUsuarioComando(
                1L, "Carlos Gomez", "carlos@aceros.com", null, Rol.ANALISTA);
        Usuario actualizado = service.actualizar(10L, cmd);

        assertThat(actualizado.nombre()).isEqualTo("Carlos Gomez");
        assertThat(actualizado.rol()).isEqualTo(Rol.ANALISTA);
        assertThat(actualizado.passwordHash()).isEqualTo("OLD_HASH");
    }

    @Test
    void actualizarRechazaCorreoDuplicado() {
        empresaExiste();
        Usuario actual = new Usuario(10L, 1L, "Carlos", "carlos@aceros.com", "OLD_HASH", Rol.LIDER, true);
        when(usuarios.buscarPorId(10L)).thenReturn(Optional.of(actual));
        when(usuarios.existePorEmail("otro@aceros.com")).thenReturn(true);

        ActualizarUsuarioComando cmd = new ActualizarUsuarioComando(
                1L, "Carlos Gomez", "otro@aceros.com", null, Rol.ANALISTA);

        assertThatThrownBy(() -> service.actualizar(10L, cmd))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("Ya existe un usuario con el correo");
    }

    @Test
    void actualizarRechazaUsuarioSinEmpresaSiNoEsAdmin() {
        Usuario actual = new Usuario(10L, 1L, "Carlos", "carlos@aceros.com", "OLD_HASH", Rol.LIDER, true);
        when(usuarios.buscarPorId(10L)).thenReturn(Optional.of(actual));

        ActualizarUsuarioComando cmd = new ActualizarUsuarioComando(
                null, "Carlos Gomez", "carlos@aceros.com", null, Rol.ANALISTA);

        assertThatThrownBy(() -> service.actualizar(10L, cmd))
                .isInstanceOf(DominioException.class)
                .hasMessageContaining("deben pertenecer a una empresa");
    }
}
