package com.indux.induxbackend.infrastructure.adapter.in.web;

import com.indux.induxbackend.application.port.in.EmpresaUseCase;
import com.indux.induxbackend.application.port.in.ProcesoUseCase;
import com.indux.induxbackend.application.port.in.ProductoUseCase;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.domain.model.Producto;
import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.infrastructure.adapter.in.web.controller.EmpresaController;
import com.indux.induxbackend.infrastructure.adapter.in.web.controller.ProcesoController;
import com.indux.induxbackend.infrastructure.adapter.in.web.controller.ProductoController;
import com.indux.induxbackend.infrastructure.adapter.in.web.exception.ManejadorErrores;
import com.indux.induxbackend.infrastructure.security.AccesoPorEmpresa;
import com.indux.induxbackend.infrastructure.security.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiRestTest {

    private final EmpresaUseCase empresas = mock(EmpresaUseCase.class);
    private final ProductoUseCase productos = mock(ProductoUseCase.class);
    private final ProcesoUseCase procesos = mock(ProcesoUseCase.class);

    private final Authentication admin = autenticado(null, Rol.ADMIN);

    private MockMvc mvc;

    private static Authentication autenticado(Long empresaId, Rol rol) {
        return new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(1L, empresaId, "x@indux.com", rol), null, List.of());
    }

    @BeforeEach
    void preparar() {
        AccesoPorEmpresa acceso = new AccesoPorEmpresa(productos);
        mvc = MockMvcBuilders
                .standaloneSetup(new EmpresaController(empresas, acceso),
                        new ProductoController(productos, acceso),
                        new ProcesoController(procesos, acceso))
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    @Test
    void crearEmpresaDevuelve201ConLocation() throws Exception {
        when(empresas.crear(any())).thenReturn(new Empresa(7L, "Aceros del Caribe", "Manufactura", true));

        mvc.perform(post("/api/empresas").principal(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Aceros del Caribe\",\"sector\":\"Manufactura\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/empresas/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nombre").value("Aceros del Caribe"));
    }

    @Test
    void campoObligatorioFaltanteDevuelve400ConDetalle() throws Exception {
        mvc.perform(post("/api/empresas").principal(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sector\":\"Manufactura\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());
    }

    @Test
    void productoDuplicadoDevuelve409() throws Exception {
        when(productos.crear(any())).thenThrow(new ConflictoException("Ya existe un producto con el código P-01"));

        mvc.perform(post("/api/productos").principal(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empresaId\":1,\"codigo\":\"P-01\",\"nombre\":\"Tornillo\",\"unidadMedida\":\"unidades\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ya existe un producto con el código P-01"));
    }

    @Test
    void productoInexistenteDevuelve404() throws Exception {
        when(productos.obtener(99L)).thenThrow(new NoEncontradoException("No existe el producto con id 99"));

        mvc.perform(get("/api/productos/99").principal(admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarProductosPorEmpresa() throws Exception {
        when(productos.listarPorEmpresa(1L))
                .thenReturn(List.of(new Producto(5L, 1L, "P-01", "Tornillo", "unidades")));

        mvc.perform(get("/api/productos").param("empresaId", "1").principal(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("P-01"));
    }

    @Test
    void unAnalistaPuedeListarLosProductosDeSuEmpresa() throws Exception {
        when(productos.listarPorEmpresa(1L))
                .thenReturn(List.of(new Producto(5L, 1L, "P-01", "Tornillo", "unidades")));

        mvc.perform(get("/api/productos").param("empresaId", "1")
                        .principal(autenticado(1L, Rol.ANALISTA)))
                .andExpect(status().isOk());
    }

    @Test
    void unAnalistaNoPuedeVerLosProductosDeOtraEmpresa() throws Exception {
        mvc.perform(get("/api/productos").param("empresaId", "1")
                        .principal(autenticado(2L, Rol.ANALISTA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unLiderNoPuedeVerUnProductoDeOtraEmpresa() throws Exception {
        when(productos.obtener(5L)).thenReturn(new Producto(5L, 1L, "P-01", "Tornillo", "unidades"));

        mvc.perform(get("/api/productos/5").principal(autenticado(2L, Rol.LIDER)))
                .andExpect(status().isForbidden());
    }
}
