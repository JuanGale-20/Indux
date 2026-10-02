package com.indux.induxbackend.infrastructure.security;

import com.indux.induxbackend.application.port.in.EmpresaUseCase;
import com.indux.induxbackend.application.port.in.EmpresaUseCase.CrearEmpresaComando;
import com.indux.induxbackend.application.port.in.UsuarioUseCase;
import com.indux.induxbackend.application.port.in.UsuarioUseCase.RegistrarUsuarioComando;
import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.domain.model.Rol;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class SeguridadIntegracionTest {

    @Autowired
    WebApplicationContext contexto;
    @Autowired
    @Qualifier("springSecurityFilterChain")
    Filter filtroSeguridad;
    @Autowired
    UsuarioUseCase usuarios;
    @Autowired
    EmpresaUseCase empresas;

    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtroSeguridad).build();
    }

    private String token(String email, String password) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$.token");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/empresas/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInventadoDevuelve401() throws Exception {
        mvc.perform(get("/api/empresas/1").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginConClaveIncorrectaDevuelve401() throws Exception {
        usuarios.registrar(new RegistrarUsuarioComando(null, "Root", "it-admin@indux.test", "secreto123", Rol.ADMIN));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"it-admin@indux.test\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elAdministradorPuedeCrearEmpresas() throws Exception {
        usuarios.registrar(new RegistrarUsuarioComando(null, "Root", "it-admin@indux.test", "secreto123", Rol.ADMIN));
        String token = token("it-admin@indux.test", "secreto123");

        mvc.perform(post("/api/empresas")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Empresa de prueba\",\"sector\":\"Manufactura\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void unAnalistaNoPuedeCrearEmpresas() throws Exception {
        Empresa empresa = empresas.crear(new CrearEmpresaComando("Aceros", "Manufactura"));
        usuarios.registrar(new RegistrarUsuarioComando(
                empresa.id(), "Ana", "it-analista@indux.test", "secreto123", Rol.ANALISTA));
        String token = token("it-analista@indux.test", "secreto123");

        mvc.perform(post("/api/empresas")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Otra\",\"sector\":\"Manufactura\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unAnalistaSoloVeSuPropiaEmpresa() throws Exception {
        Empresa propia = empresas.crear(new CrearEmpresaComando("Aceros", "Manufactura"));
        Empresa ajena = empresas.crear(new CrearEmpresaComando("Textiles", "Manufactura"));
        usuarios.registrar(new RegistrarUsuarioComando(
                propia.id(), "Ana", "it-analista@indux.test", "secreto123", Rol.ANALISTA));
        String token = token("it-analista@indux.test", "secreto123");

        mvc.perform(get("/api/empresas/" + propia.id()).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/empresas/" + ajena.id()).header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }
}