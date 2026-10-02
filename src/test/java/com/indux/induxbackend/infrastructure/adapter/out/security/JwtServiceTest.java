package com.indux.induxbackend.infrastructure.adapter.out.security;

import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.Usuario;
import com.indux.induxbackend.infrastructure.security.UsuarioAutenticado;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRETO = "clave-de-pruebas-con-mas-de-32-caracteres!!";
    private final JwtService jwt = new JwtService(SECRETO, 60);

    private final Usuario analista =
            new Usuario(7L, 3L, "Ana", "ana@aceros.com", "HASH", Rol.ANALISTA, true);

    @Test
    void generaYLeeElToken() {
        UsuarioAutenticado u = jwt.leer(jwt.generar(analista)).orElseThrow();

        assertThat(u.id()).isEqualTo(7L);
        assertThat(u.empresaId()).isEqualTo(3L);
        assertThat(u.email()).isEqualTo("ana@aceros.com");
        assertThat(u.rol()).isEqualTo(Rol.ANALISTA);
    }

    @Test
    void elAdministradorNoTieneEmpresa() {
        Usuario admin = new Usuario(1L, null, "Root", "root@indux.com", "HASH", Rol.ADMIN, true);

        UsuarioAutenticado u = jwt.leer(jwt.generar(admin)).orElseThrow();

        assertThat(u.empresaId()).isNull();
        assertThat(u.rol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        JwtService otro = new JwtService("otra-clave-distinta-con-mas-de-32-caracteres!", 60);

        assertThat(jwt.leer(otro.generar(analista))).isEmpty();
    }

    @Test
    void rechazaUnTokenExpirado() {
        JwtService expirado = new JwtService(SECRETO, -1);

        assertThat(jwt.leer(expirado.generar(analista))).isEmpty();
    }

    @Test
    void rechazaTextoQueNoEsUnToken() {
        assertThat(jwt.leer("no-es-un-token")).isEmpty();
    }

    @Test
    void rechazaUnSecretoCorto() {
        assertThatThrownBy(() -> new JwtService("corto", 60))
                .isInstanceOf(IllegalStateException.class);
    }
}