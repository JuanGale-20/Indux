package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.ProcesoUseCase.CrearProcesoComando;
import com.indux.induxbackend.application.port.out.ProcesoRepositoryPort;
import com.indux.induxbackend.application.port.out.ProductoRepositoryPort;
import com.indux.induxbackend.application.port.out.UsuarioRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.Proceso;
import com.indux.induxbackend.domain.model.Producto;
import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProcesoServiceTest {

    private final ProcesoRepositoryPort procesos = mock(ProcesoRepositoryPort.class);
    private final ProductoRepositoryPort productos = mock(ProductoRepositoryPort.class);
    private final UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
    private final ProcesoService service = new ProcesoService(procesos, productos, usuarios);

    private final CrearProcesoComando sinLider =
            new CrearProcesoComando(5L, null, "COR", "Corte", 1, "unidades");
    private final CrearProcesoComando conLider =
            new CrearProcesoComando(5L, 9L, "COR", "Corte", 1, "unidades");

    private void productoExiste() {
        // El producto pertenece a la empresa 1
        when(productos.buscarPorId(5L))
                .thenReturn(Optional.of(new Producto(5L, 1L, "P-01", "Tornillo", "unidades")));
    }

    private void liderExiste(Long empresaId, Rol rol, boolean activo) {
        when(usuarios.buscarPorId(9L)).thenReturn(Optional.of(
                new Usuario(9L, empresaId, "Ana", "ana@aceros.com", "HASH", rol, activo)));
    }

    @Test
    void creaElProcesoSiTodoEsValido() {
        productoExiste();
        when(procesos.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Proceso creado = service.crear(sinLider);

        assertThat(creado.codigo()).isEqualTo("COR");
        verify(procesos).guardar(any());
    }

    @Test
    void rechazaUnProductoInexistente() {
        when(productos.buscarPorId(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(sinLider)).isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void rechazaUnCodigoDuplicadoEnElProducto() {
        productoExiste();
        when(procesos.existePorProductoYCodigo(5L, "COR")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(sinLider))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("código");
        verify(procesos, never()).guardar(any());
    }

    @Test
    void rechazaUnaSecuenciaDuplicadaEnElProducto() {
        productoExiste();
        when(procesos.existePorProductoYSecuencia(5L, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.crear(sinLider))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("secuencia");
        verify(procesos, never()).guardar(any());
    }

    @Test
    void aceptaUnLiderActivoDeLaMismaEmpresa() {
        productoExiste();
        liderExiste(1L, Rol.LIDER, true);
        when(procesos.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Proceso creado = service.crear(conLider);

        assertThat(creado.liderId()).isEqualTo(9L);
    }

    @Test
    void rechazaUnLiderInexistente() {
        productoExiste();
        when(usuarios.buscarPorId(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(conLider)).isInstanceOf(NoEncontradoException.class);
        verify(procesos, never()).guardar(any());
    }

    @Test
    void rechazaUnUsuarioQueNoTieneRolLider() {
        productoExiste();
        liderExiste(1L, Rol.ANALISTA, true);

        assertThatThrownBy(() -> service.crear(conLider))
                .isInstanceOf(DominioException.class)
                .hasMessageContaining("Líder");
        verify(procesos, never()).guardar(any());
    }

    @Test
    void rechazaUnLiderDeOtraEmpresa() {
        productoExiste();
        liderExiste(2L, Rol.LIDER, true);

        assertThatThrownBy(() -> service.crear(conLider))
                .isInstanceOf(DominioException.class)
                .hasMessageContaining("otra empresa");
        verify(procesos, never()).guardar(any());
    }

    @Test
    void listarProcesosDeUnProductoInexistenteDa404() {
        when(productos.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listarPorProducto(99L))
                .isInstanceOf(NoEncontradoException.class);
    }
}