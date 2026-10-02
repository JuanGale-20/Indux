package com.indux.induxbackend.domain.model;
import com.indux.induxbackend.domain.exception.DominioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ModeloDominioTest {
    @Test
    void productoValidoSeCreaConTextosSinEspacios() {
        Producto p = new Producto(null, 1L, "  P-01 ", " Tornillo ", "unidades");

        assertThat(p.codigo()).isEqualTo("P-01");
        assertThat(p.nombre()).isEqualTo("Tornillo");
    }

    @Test
    void productoConCodigoVacioSeRechaza() {
        assertThatThrownBy(() -> new Producto(null, 1L, "  ", "Tornillo", "unidades"))
                .isInstanceOf(DominioException.class)
                .hasMessage("El código del producto no puede estar vacío");
    }

    @Test
    void procesoConSecuenciaCeroSeRechaza() {
        assertThatThrownBy(() -> new Proceso(null, 1L, null, "CORTE", "Corte", 0, "unidades"))
                .isInstanceOf(DominioException.class)
                .hasMessage("La secuencia del proceso debe ser mayor que 0");
    }

    @Test
    void empresaSinNombreSeRechaza() {
        assertThatThrownBy(() -> new Empresa(null, "", "Manufactura", true))
                .isInstanceOf(DominioException.class);
    }
}
