package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.Proceso;

import java.math.BigDecimal;
import java.util.List;

public interface ProcesoUseCase {

    Proceso crear(CrearProcesoComando comando);

    Proceso obtener(Long id);

    List<Proceso> listarPorProducto(Long productoId);

    Proceso actualizar(Long id, ActualizarProcesoComando comando);

    void eliminar(Long id);

    record CrearProcesoComando(Long productoId, Long liderId, String codigo,
                               String nombre, int secuencia, String unidadProcesada,
                               BigDecimal merma, String unidadEntrada) {

        /** Constructor anterior: sin merma ni unidad de entrada. */
        public CrearProcesoComando(Long productoId, Long liderId, String codigo,
                                   String nombre, int secuencia, String unidadProcesada) {
            this(productoId, liderId, codigo, nombre, secuencia, unidadProcesada, null, null);
        }
    }

    /**
     * Si merma o unidadEntrada vienen nulas se conserva el valor actual.
     * Para borrar la unidad de entrada se envía una cadena vacía.
     */
    record ActualizarProcesoComando(Long liderId, String codigo, String nombre,
                                    int secuencia, String unidadProcesada,
                                    BigDecimal merma, String unidadEntrada) {

        /** Constructor anterior: conserva la merma y la unidad de entrada actuales. */
        public ActualizarProcesoComando(Long liderId, String codigo, String nombre,
                                        int secuencia, String unidadProcesada) {
            this(liderId, codigo, nombre, secuencia, unidadProcesada, null, null);
        }
    }
}