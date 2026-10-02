package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.ProductoUseCase.CrearProductoComando;
import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.application.port.out.ProductoRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.domain.model.Producto;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    private final ProductoRepositoryPort productos = mock(ProductoRepositoryPort.class);
    private final EmpresaRepositoryPort empresas = mock(EmpresaRepositoryPort.class);
    private final ProductoService service = new ProductoService(productos, empresas);

    private final CrearProductoComando comando =
            new CrearProductoComando(1L, " P-01 ", "Tornillo", "unidades");

    @Test
    void creaElProductoSiLaEmpresaExisteYElCodigoEsNuevo() {
        when(empresas.buscarPorId(1L)).thenReturn(Optional.of(new Empresa(1L, "Aceros", null, true)));
        when(productos.existePorEmpresaYCodigo(1L, "P-01")).thenReturn(false);
        when(productos.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Producto creado = service.crear(comando);

        assertThat(creado.codigo()).isEqualTo("P-01");
        verify(productos).guardar(any());
    }

    @Test
    void rechazaUnCodigoDuplicadoEnLaMismaEmpresa() {
        when(empresas.buscarPorId(1L)).thenReturn(Optional.of(new Empresa(1L, "Aceros", null, true)));
        when(productos.existePorEmpresaYCodigo(1L, "P-01")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(comando)).isInstanceOf(ConflictoException.class);
        verify(productos, never()).guardar(any());
    }

    @Test
    void rechazaUnaEmpresaInexistente() {
        when(empresas.buscarPorId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(comando)).isInstanceOf(NoEncontradoException.class);
        verify(productos, never()).guardar(any());
    }
}
