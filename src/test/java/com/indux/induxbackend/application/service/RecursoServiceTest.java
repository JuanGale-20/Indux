package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.RecursoUseCase.CrearRecursoComando;
import com.indux.induxbackend.application.port.in.RecursoUseCase.VincularProcesoComando;
import com.indux.induxbackend.application.port.out.EmpresaRepositoryPort;
import com.indux.induxbackend.application.port.out.ProcesoRecursoRepositoryPort;
import com.indux.induxbackend.application.port.out.ProcesoRepositoryPort;
import com.indux.induxbackend.application.port.out.ProductoRepositoryPort;
import com.indux.induxbackend.application.port.out.RecursoRepositoryPort;
import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.model.Empresa;
import com.indux.induxbackend.domain.model.Proceso;
import com.indux.induxbackend.domain.model.ProcesoRecurso;
import com.indux.induxbackend.domain.model.Producto;
import com.indux.induxbackend.domain.model.Recurso;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecursoServiceTest {

    private final RecursoRepositoryPort recursos = mock(RecursoRepositoryPort.class);
    private final EmpresaRepositoryPort empresas = mock(EmpresaRepositoryPort.class);
    private final ProcesoRecursoRepositoryPort vinculos = mock(ProcesoRecursoRepositoryPort.class);
    private final ProcesoRepositoryPort procesos = mock(ProcesoRepositoryPort.class);
    private final ProductoRepositoryPort productos = mock(ProductoRepositoryPort.class);

    private final RecursoService servicio =
            new RecursoService(recursos, empresas, vinculos, procesos, productos);

    private void datosDeVinculo(long empresaDelProducto) {
        when(recursos.buscarPorId(3L))
                .thenReturn(Optional.of(new Recurso(3L, 1L, "M1", "Torno", null, 1, null)));
        when(procesos.buscarPorId(7L))
                .thenReturn(Optional.of(new Proceso(7L, 5L, null, "COR", "Corte", 1, "unidades")));
        when(productos.buscarPorId(5L))
                .thenReturn(Optional.of(new Producto(5L, empresaDelProducto, "P-01", "Tornillo", "unidades")));
    }

    @Test
    void noPermiteCrearUnRecursoConCodigoRepetido() {
        when(empresas.buscarPorId(1L)).thenReturn(Optional.of(mock(Empresa.class)));
        when(recursos.existePorEmpresaYCodigo(1L, "M1")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(
                new CrearRecursoComando(1L, "M1", "Torno", null, 1, null)))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void vinculaUnRecursoAUnProcesoDeLaMismaEmpresa() {
        datosDeVinculo(1L);
        when(vinculos.existe(7L, 3L)).thenReturn(false);
        when(vinculos.vincular(any(ProcesoRecurso.class))).thenAnswer(i -> i.getArgument(0));

        ProcesoRecurso v = servicio.vincular(3L, new VincularProcesoComando(7L, null));

        assertThat(v.procesoId()).isEqualTo(7L);
        verify(vinculos).vincular(any(ProcesoRecurso.class));
    }

    @Test
    void rechazaVincularUnProcesoDeOtraEmpresa() {
        datosDeVinculo(2L);

        assertThatThrownBy(() -> servicio.vincular(3L, new VincularProcesoComando(7L, null)))
                .isInstanceOf(DominioException.class);
    }

    @Test
    void rechazaVincularDosVeces() {
        datosDeVinculo(1L);
        when(vinculos.existe(7L, 3L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.vincular(3L, new VincularProcesoComando(7L, null)))
                .isInstanceOf(ConflictoException.class);
    }
}