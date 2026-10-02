package com.indux.induxbackend.infrastructure.adapter.out.persistence;

import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.application.port.out.ProcesoRepositoryPort;
import com.indux.induxbackend.application.port.out.ProductoRepositoryPort;
import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.domain.model.Proceso;
import com.indux.induxbackend.domain.model.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // cada test se revierte: no deja datos en la base
class PersistenciaAdaptersTest {

    @Autowired
    EmpresaRepositoryPort empresas;
    @Autowired
    ProductoRepositoryPort productos;
    @Autowired
    ProcesoRepositoryPort procesos;

    @Test
    void guardaYLeeEmpresaProductoYProcesosOrdenados() {
        Empresa empresa = empresas.guardar(new Empresa(null, "Aceros del Caribe", "Manufactura", true));
        assertThat(empresa.id()).isNotNull();

        Producto producto = productos.guardar(
                new Producto(null, empresa.id(), "P-01", "Tornillo", "unidades"));
        assertThat(productos.buscarPorId(producto.id())).contains(producto);
        assertThat(productos.listarPorEmpresa(empresa.id())).containsExactly(producto);
        assertThat(productos.existePorEmpresaYCodigo(empresa.id(), "P-01")).isTrue();
        assertThat(productos.existePorEmpresaYCodigo(empresa.id(), "P-99")).isFalse();

        procesos.guardar(new Proceso(null, producto.id(), null, "ENS", "Ensamblaje", 2, "unidades"));
        procesos.guardar(new Proceso(null, producto.id(), null, "COR", "Corte", 1, "unidades"));

        assertThat(procesos.listarPorProductoOrdenadoPorSecuencia(producto.id()))
                .extracting(Proceso::codigo)
                .containsExactly("COR", "ENS");
        assertThat(procesos.existePorProductoYSecuencia(producto.id(), 2)).isTrue();
        assertThat(procesos.existePorProductoYCodigo(producto.id(), "XXX")).isFalse();
    }
}
