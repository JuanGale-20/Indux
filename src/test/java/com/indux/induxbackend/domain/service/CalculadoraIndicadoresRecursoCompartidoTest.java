package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.model.*;
import com.indux.induxbackend.domain.service.CalculadoraRecursoCompartido.DatosCalculoRecurso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraIndicadoresRecursoCompartidoTest {

    private final CalculadoraRecursoCompartido calculadora = new CalculadoraRecursoCompartido();

    private Recurso crearRecurso(Long id, int cantidad, EstadoRecurso estado) {
        return new Recurso(id, 1L, "REC-" + id, "Recurso " + id, "Maquina", cantidad, estado);
    }

    private ReporteRecurso crearLinea(Long id, Long recursoId, BigDecimal tiempoProc,
                                      BigDecimal tiempoNoDisp, int turnos, BigDecimal horasTurno, int dias) {
        return new ReporteRecurso(id, 1L, recursoId, tiempoProc, "kg",
                new BigDecimal("1000"), tiempoNoDisp, turnos, horasTurno, dias);
    }

    @Test
    @DisplayName("Un solo producto, demanda 1000, tiempo 0.2 -> requeridas 200, utilización 62.5, BAJA_UTILIZACION, prioridad nula")
    void caso1UnSoloProducto() {
        Recurso r = crearRecurso(1L, 1, EstadoRecurso.DISPONIBLE);
        ReporteRecurso l = crearLinea(10L, 1L, new BigDecimal("0.2"), BigDecimal.ZERO, 2, new BigDecimal("8"), 20);

        DatosCalculoRecurso datos = new DatosCalculoRecurso(l, r, new BigDecimal("200"), new BigDecimal("200"), 0);
        List<AnalisisRecursoReporte> resultado = calculadora.calcularYPriorizar(List.of(datos));

        assertThat(resultado).hasSize(1);
        AnalisisRecursoReporte a = resultado.get(0);
        assertThat(a.horasDisponibles()).isEqualByComparingTo("320.0000");
        assertThat(a.horasRequeridas()).isEqualByComparingTo("200.0000");
        assertThat(a.utilizacion()).isEqualByComparingTo("62.50");
        assertThat(a.clasificacion()).isEqualTo(ClasificacionRestriccion.BAJA_UTILIZACION);
        assertThat(a.prioridad()).isNull();
    }

    @Test
    @DisplayName("Recurso compartido: producto 1 aporta 200 h y producto 2 aporta 160 h -> requeridas 360, utilización 112.5, SOBRECARGA, déficit 40 h, impacto 11.11 %, deficitCapacidad 200, prioridad 1")
    void caso2RecursoCompartido() {
        Recurso r = crearRecurso(1L, 1, EstadoRecurso.DISPONIBLE);
        ReporteRecurso l = crearLinea(10L, 1L, new BigDecimal("0.2"), BigDecimal.ZERO, 2, new BigDecimal("8"), 20);

        DatosCalculoRecurso datos = new DatosCalculoRecurso(l, r, new BigDecimal("200"), new BigDecimal("360"), 1);
        List<AnalisisRecursoReporte> resultado = calculadora.calcularYPriorizar(List.of(datos));

        assertThat(resultado).hasSize(1);
        AnalisisRecursoReporte a = resultado.get(0);
        assertThat(a.horasDisponibles()).isEqualByComparingTo("320.0000");
        assertThat(a.horasRequeridas()).isEqualByComparingTo("360.0000");
        assertThat(a.aporteHoras()).isEqualByComparingTo("200.0000");
        assertThat(a.utilizacion()).isEqualByComparingTo("112.50");
        assertThat(a.clasificacion()).isEqualTo(ClasificacionRestriccion.SOBRECARGA);
        assertThat(a.deficitHoras()).isEqualByComparingTo("40.0000");
        assertThat(a.impacto()).isEqualByComparingTo("11.11");
        assertThat(a.deficitCapacidad()).isEqualByComparingTo("200.0000");
        assertThat(a.prioridad()).isEqualTo(1);
        assertThat(a.diagnostico()).contains("Alerta crítica: sobrecarga")
                .contains("Recurso compartido con 1 producto más");
    }

    @Test
    @DisplayName("Merma del otro producto del 20 % (demanda 800): aporta 200 h -> requeridas totales 400")
    void caso3MermaOtroProducto() {
        // Validación con AnalizadorReporte
        ConversorUnidades conversor = new ConversorUnidades(List.of());
        CalculadoraEntradas calcEntradas = new CalculadoraEntradas();
        CalculadoraAporte calcAporte = new CalculadoraAporte();

        Proceso p1 = new Proceso(1L, 2L, null, "P1", "Proceso 1", 1, "kg", new BigDecimal("20"), null);
        var entradas = calcEntradas.calcular(new BigDecimal("800"), "kg", List.of(p1), conversor);

        ReporteRecurso lineaOtro = crearLinea(20L, 1L, new BigDecimal("0.2"), BigDecimal.ZERO, 2, new BigDecimal("8"), 20);
        BigDecimal aporteOtro = calcAporte.calcular(lineaOtro, List.of(1L), entradas, conversor);
        assertThat(aporteOtro).isEqualByComparingTo("200.0000");

        BigDecimal total = new BigDecimal("200").add(aporteOtro);
        assertThat(total).isEqualByComparingTo("400.0000");
    }

    @Test
    @DisplayName("Tiempo no disponible 20 h con demanda 1000 y tiempo 0.3 -> disponibles 300, requeridas 300, utilización 100, CAPACIDAD_AJUSTADA")
    void caso4TiempoNoDisponible() {
        Recurso r = crearRecurso(1L, 1, EstadoRecurso.DISPONIBLE);
        ReporteRecurso l = crearLinea(10L, 1L, new BigDecimal("0.3"), new BigDecimal("20"), 2, new BigDecimal("8"), 20);

        DatosCalculoRecurso datos = new DatosCalculoRecurso(l, r, new BigDecimal("300"), new BigDecimal("300"), 0);
        List<AnalisisRecursoReporte> resultado = calculadora.calcularYPriorizar(List.of(datos));

        assertThat(resultado).hasSize(1);
        AnalisisRecursoReporte a = resultado.get(0);
        assertThat(a.horasDisponibles()).isEqualByComparingTo("300.0000");
        assertThat(a.horasRequeridas()).isEqualByComparingTo("300.0000");
        assertThat(a.utilizacion()).isEqualByComparingTo("100.00");
        assertThat(a.clasificacion()).isEqualTo(ClasificacionRestriccion.CAPACIDAD_AJUSTADA);
        assertThat(a.prioridad()).isNull();
    }

    @Test
    @DisplayName("Tres recursos con déficit/sin déficit: el de mayor impacto es prioridad 1, el siguiente 2, el que no tiene déficit queda con prioridad nula")
    void caso5PriorizacionMultiplesRecursos() {
        Recurso r1 = crearRecurso(1L, 1, EstadoRecurso.DISPONIBLE);
        Recurso r2 = crearRecurso(2L, 1, EstadoRecurso.DISPONIBLE);
        Recurso r3 = crearRecurso(3L, 1, EstadoRecurso.DISPONIBLE);

        ReporteRecurso l1 = crearLinea(10L, 1L, new BigDecimal("0.2"), BigDecimal.ZERO, 2, new BigDecimal("8"), 20); // 320 disp
        ReporteRecurso l2 = crearLinea(20L, 2L, new BigDecimal("0.2"), BigDecimal.ZERO, 2, new BigDecimal("8"), 20); // 320 disp
        ReporteRecurso l3 = crearLinea(30L, 3L, new BigDecimal("0.2"), BigDecimal.ZERO, 2, new BigDecimal("8"), 20); // 320 disp

        // R1: 400 req -> deficit 80, impacto 80*100/400 = 20%
        DatosCalculoRecurso d1 = new DatosCalculoRecurso(l1, r1, new BigDecimal("400"), new BigDecimal("400"), 0);
        // R2: 360 req -> deficit 40, impacto 40*100/360 = 11.11%
        DatosCalculoRecurso d2 = new DatosCalculoRecurso(l2, r2, new BigDecimal("360"), new BigDecimal("360"), 0);
        // R3: 200 req -> deficit 0, sin deficit
        DatosCalculoRecurso d3 = new DatosCalculoRecurso(l3, r3, new BigDecimal("200"), new BigDecimal("200"), 0);

        List<AnalisisRecursoReporte> resultado = calculadora.calcularYPriorizar(List.of(d2, d3, d1));

        assertThat(resultado).hasSize(3);
        assertThat(resultado.get(0).recursoId()).isEqualTo(1L);
        assertThat(resultado.get(0).prioridad()).isEqualTo(1);

        assertThat(resultado.get(1).recursoId()).isEqualTo(2L);
        assertThat(resultado.get(1).prioridad()).isEqualTo(2);

        assertThat(resultado.get(2).recursoId()).isEqualTo(3L);
        assertThat(resultado.get(2).prioridad()).isNull();
    }

    @Test
    @DisplayName("Recurso con cantidad 2 -> 640 h disponibles; demanda 1000 y tiempo 0.5 -> utilización 78.13")
    void caso6CantidadDosRecursos() {
        Recurso r = crearRecurso(1L, 2, EstadoRecurso.DISPONIBLE);
        ReporteRecurso l = crearLinea(10L, 1L, new BigDecimal("0.5"), BigDecimal.ZERO, 2, new BigDecimal("8"), 20);

        // Horas requeridas = 1000 * 0.5 = 500
        DatosCalculoRecurso datos = new DatosCalculoRecurso(l, r, new BigDecimal("500"), new BigDecimal("500"), 0);
        List<AnalisisRecursoReporte> resultado = calculadora.calcularYPriorizar(List.of(datos));

        assertThat(resultado).hasSize(1);
        AnalisisRecursoReporte a = resultado.get(0);
        assertThat(a.horasDisponibles()).isEqualByComparingTo("640.0000");
        assertThat(a.horasRequeridas()).isEqualByComparingTo("500.0000");
        assertThat(a.utilizacion()).isEqualByComparingTo("78.13");
        assertThat(a.clasificacion()).isEqualTo(ClasificacionRestriccion.BAJA_UTILIZACION);
    }
}
