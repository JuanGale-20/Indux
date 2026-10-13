package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.EmpresaUseCase.ActualizarEmpresaComando;
import com.indux.induxbackend.application.port.in.ProcesoUseCase.ActualizarProcesoComando;
import com.indux.induxbackend.application.port.in.ProductoUseCase.ActualizarProductoComando;
import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.application.port.out.PasswordHasherPort;
import com.indux.induxbackend.application.port.out.ProcesoRepositoryPort;
import com.indux.induxbackend.application.port.out.ProductoRepositoryPort;
import com.indux.induxbackend.application.port.out.UsuarioRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.domain.model.Proceso;
import com.indux.induxbackend.domain.model.Producto;
import com.indux.induxbackend.domain.model.Rol;
import com.indux.induxbackend.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GestionServiciosTest {

    private final EmpresaRepositoryPort empresas = mock(EmpresaRepositoryPort.class);
    private final UsuarioRepositoryPort usuarios = mock(UsuarioRepositoryPort.class);
    private final ProductoRepositoryPort productos = mock(ProductoRepositoryPort.class);
    private final ProcesoRepositoryPort procesos = mock(ProcesoRepositoryPort.class);

    @Test
    void desactivarUnaEmpresaConservaSusDatos() {
        when(empresas.buscarPorId(1L)).thenReturn(Optional.of(new Empresa(1L, "Aceros", "metal", true)));
        when(empresas.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        new EmpresaService(empresas).desactivar(1L);

        ArgumentCaptor<Empresa> captor = ArgumentCaptor.forClass(Empresa.class);
        verify(empresas).guardar(captor.capture());
        assertThat(captor.getValue().activa()).isFalse();
        assertThat(captor.getValue().nombre()).isEqualTo("Aceros");
    }

    @Test
    void noSeDesactivaAUnAdministrador() {
        UsuarioService service = new UsuarioService(usuarios, empresas, mock(PasswordHasherPort.class));
        when(usuarios.buscarPorId(2L)).thenReturn(
                Optional.of(new Usuario(2L, null, "Admin", "admin@indux.com", "HASH", Rol.ADMIN, true)));

        assertThatThrownBy(() -> service.desactivar(2L)).isInstanceOf(ConflictoException.class);
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void actualizarProductoRechazaUnCodigoQueYaExiste() {
        ProductoService service = new ProductoService(productos, empresas);
        when(productos.buscarPorId(5L))
                .thenReturn(Optional.of(new Producto(5L, 1L, "P-01", "Tornillo", "unidades")));
        when(productos.existePorEmpresaYCodigo(1L, "P-02")).thenReturn(true);

        assertThatThrownBy(() -> service.actualizar(5L,
                new ActualizarProductoComando("P-02", "Tornillo", "unidades")))
                .isInstanceOf(ConflictoException.class);
        verify(productos, never()).guardar(any());
    }

    @Test
    void actualizarProcesoRechazaUnaSecuenciaOcupada() {
        ProcesoService service = new ProcesoService(procesos, productos, usuarios);
        when(procesos.buscarPorId(9L))
                .thenReturn(Optional.of(new Proceso(9L, 5L, null, "COR", "Corte", 1, "unidades")));
        when(productos.buscarPorId(5L))
                .thenReturn(Optional.of(new Producto(5L, 1L, "P-01", "Tornillo", "unidades")));
        when(procesos.existePorProductoYSecuencia(5L, 2)).thenReturn(true);

        assertThatThrownBy(() -> service.actualizar(9L,
                new ActualizarProcesoComando(null, "COR", "Corte", 2, "unidades")))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void eliminarUnProductoInexistenteDaNoEncontrado() {
        ProductoService service = new ProductoService(productos, empresas);
        when(productos.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L)).isInstanceOf(NoEncontradoException.class);
        verify(productos, never()).eliminar(any());
    }
}