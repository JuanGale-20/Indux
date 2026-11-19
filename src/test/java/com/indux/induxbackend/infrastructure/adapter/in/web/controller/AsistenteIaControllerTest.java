package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indux.induxbackend.application.port.in.AsistenteIaUseCase;
import com.indux.induxbackend.application.port.in.EmpresaUseCase;
import com.indux.induxbackend.application.port.in.EmpresaUseCase.CrearEmpresaComando;
import com.indux.induxbackend.application.port.in.UsuarioUseCase;
import com.indux.induxbackend.application.port.in.UsuarioUseCase.RegistrarUsuarioComando;
import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.asistente.RespuestaAsistente;
import com.indux.induxbackend.infrastructure.adapter.in.web.dto.ConsultaAsistenteRequest;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AsistenteIaControllerTest {

    @Autowired
    WebApplicationContext contexto;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    Filter filtroSeguridad;

    @Autowired
    UsuarioUseCase usuarios;

    @Autowired
    EmpresaUseCase empresas;

    @MockitoBean
    private AsistenteIaUseCase asistenteUseCase;

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

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

    @Test
    void consultaSinAutenticacionDevuelve401() throws Exception {
        var req = new ConsultaAsistenteRequest("¿Cómo interpretar SteelForce?", null, List.of());

        mvc.perform(post("/api/asistente/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consultaConAutenticacionValidaDevuelve200YRespuesta() throws Exception {
        Empresa empresa = empresas.crear(new CrearEmpresaComando("Empresa IA Test", "Manufactura"));
        usuarios.registrar(new RegistrarUsuarioComando(
                empresa.id(), "Ingeniero Planta", "it-ia@indux.test", "secreto123", Rol.ANALISTA));

        String token = token("it-ia@indux.test", "secreto123");

        when(asistenteUseCase.consultar(any())).thenReturn(
                new RespuestaAsistente("Análisis detallado de SteelForce", "gemini-3.8-flash", true, List.of("Reporte #12"))
        );

        var req = new ConsultaAsistenteRequest("¿Cuál es el cuello de botella?", 12L, List.of());

        mvc.perform(post("/api/asistente/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta").value("Análisis detallado de SteelForce"))
                .andExpect(jsonPath("$.modelo").value("gemini-3.8-flash"))
                .andExpect(jsonPath("$.proveedorConfigurado").value(true))
                .andExpect(jsonPath("$.motor").value("GEMINI"));
    }

    @Test
    void consultaConMensajeEnBlancoDevuelve400() throws Exception {
        Empresa empresa = empresas.crear(new CrearEmpresaComando("Empresa IA Test 2", "Manufactura"));
        usuarios.registrar(new RegistrarUsuarioComando(
                empresa.id(), "Ingeniero Planta", "it-ia2@indux.test", "secreto123", Rol.ANALISTA));

        String token = token("it-ia2@indux.test", "secreto123");

        var req = new ConsultaAsistenteRequest("   ", null, List.of());

        mvc.perform(post("/api/asistente/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}

