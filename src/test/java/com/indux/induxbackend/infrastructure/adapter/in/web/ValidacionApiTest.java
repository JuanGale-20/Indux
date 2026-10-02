package com.indux.induxbackend.infrastructure.adapter.in.web;

import com.indux.induxbackend.application.service.ValidacionService;
import com.indux.induxbackend.domain.validacion.CatalogosSteelForce;
import com.indux.induxbackend.domain.validacion.ValidadorEmpresa;
import com.indux.induxbackend.domain.validacion.ValidadorProceso;
import com.indux.induxbackend.domain.validacion.ValidadorProducto;
import com.indux.induxbackend.infrastructure.adapter.in.web.controller.ValidacionController;
import com.indux.induxbackend.infrastructure.adapter.in.web.exception.ManejadorErrores;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ValidacionApiTest {

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new ValidacionController(new ValidacionService(
                    new ValidadorEmpresa(CatalogosSteelForce.SECTORES),
                    new ValidadorProducto(CatalogosSteelForce.UNIDADES),
                    new ValidadorProceso(CatalogosSteelForce.UNIDADES))))
            .setControllerAdvice(new ManejadorErrores())
            .build();

    @Test
    void productoConErroresYAdvertenciasDevuelve200ConTodosLosHallazgos() throws Exception {
        mvc.perform(post("/api/validaciones/producto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"\",\"nombre\":\"\",\"unidadMedida\":\"litros\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CON_ERRORES_CRITICOS"))
                .andExpect(jsonPath("$.puedeAnalizarse").value(false))
                .andExpect(jsonPath("$.hallazgos.length()").value(3));
    }

    @Test
    void productoValidoDevuelveEstadoValido() throws Exception {
        mvc.perform(post("/api/validaciones/producto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"P-01\",\"nombre\":\"Tornillo\",\"unidadMedida\":\"unidades\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("VALIDO"))
                .andExpect(jsonPath("$.puedeAnalizarse").value(true))
                .andExpect(jsonPath("$.hallazgos.length()").value(0));
    }

    @Test
    void procesoSinSecuenciaEsErrorCritico() throws Exception {
        mvc.perform(post("/api/validaciones/proceso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"COR\",\"nombre\":\"Corte\",\"unidadProcesada\":\"unidades\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CON_ERRORES_CRITICOS"))
                .andExpect(jsonPath("$.hallazgos[0].variable").value("Secuencia del proceso"));
    }

    @Test
    void empresaSinNombreEsErrorCritico() throws Exception {
        mvc.perform(post("/api/validaciones/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sector\":\"Manufactura\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hallazgos[0].severidad").value("ERROR_CRITICO"));
    }
}
