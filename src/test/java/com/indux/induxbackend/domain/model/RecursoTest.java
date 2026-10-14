package com.indux.induxbackend.domain.model;

import com.indux.induxbackend.domain.exception.DominioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecursoTest {

    @Test
    void sinEstadoQuedaDisponibleYElTipoVacioSeVuelveNulo() {
        Recurso r = new Recurso(null, 1L, " M1 ", " Torno ", "  ", 2, null);

        assertThat(r.codigo()).isEqualTo("M1");
        assertThat(r.nombre()).isEqualTo("Torno");
        assertThat(r.tipo()).isNull();
        assertThat(r.estado()).isEqualTo(EstadoRecurso.DISPONIBLE);
    }

    @Test
    void rechazaCantidadDisponibleCeroONegativa() {
        assertThatThrownBy(() -> new Recurso(null, 1L, "M1", "Torno", "máquina", 0, null))
                .isInstanceOf(DominioException.class);
    }
}