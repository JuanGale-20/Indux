package com.indux.induxbackend.infrastructure.adapter.out.persistence;

import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.application.port.out.UsuarioRepositoryPort;
import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UsuarioPersistenciaTest {

    @Autowired
    UsuarioRepositoryPort usuarios;
    @Autowired
    EmpresaRepositoryPort empresas;

    @Test
    void guardaYBuscaUnUsuarioPorCorreo() {
        Empresa empresa = empresas.guardar(new Empresa(null, "Aceros", "Manufactura", true));

        Usuario guardado = usuarios.guardar(
                new Usuario(null, empresa.id(), "Ana", "ana@aceros.com", "HASH", Rol.LIDER, true));

        assertThat(guardado.id()).isNotNull();
        assertThat(usuarios.buscarPorEmail("ana@aceros.com")).contains(guardado);
        assertThat(usuarios.existePorEmail("ana@aceros.com")).isTrue();
        assertThat(usuarios.existePorEmail("otro@aceros.com")).isFalse();
    }
}
