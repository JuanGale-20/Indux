package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.service.ParserArchivoImportacion.ResultadoLectura;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ParserArchivoImportacionTest {

    private final ParserArchivoImportacion parser = new ParserArchivoImportacion();

    @Test
    @DisplayName("Parser CSV: separador punto y coma, con BOM UTF-8 y comillas")
    void csvConPuntoYComaBOMYComillas() throws IOException {
        String csv = "\uFEFFproducto_codigo;planta_area;frecuencia;periodo_inicio;periodo_fin;demanda;produccion_real;"
                + "recurso_codigo;tiempo_procesamiento;unidad_referencia;tiempo_calendario;tiempo_no_disponible;numero_turnos;horas_por_turno;dias_operacion\n"
                + "\"PROD-01\";\"Planta Norte\";MENSUAL;2026-01-01;2026-01-31;1000;950;REC-01;0,25;kg;720;0;2;8;20";

        ResultadoLectura res = parser.parsear(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), "test.csv");

        assertThat(res.columnasFaltantes()).isEmpty();
        assertThat(res.filas()).hasSize(1);
        assertThat(res.filas().get(0).obtener("producto_codigo")).isEqualTo("PROD-01");
        assertThat(res.filas().get(0).obtener("planta_area")).isEqualTo("Planta Norte");
    }

    @Test
    @DisplayName("Parser CSV: última fila sin salto de línea y filas vacías intermedias")
    void csvUltimaFilaSinSaltoYVacias() throws IOException {
        String csv = "producto_codigo,planta_area,frecuencia,periodo_inicio,periodo_fin,demanda,produccion_real,"
                + "recurso_codigo,tiempo_procesamiento,unidad_referencia,tiempo_calendario,numero_turnos,horas_por_turno,dias_operacion\n"
                + "\n" // vacía
                + "PROD-01,Planta,MENSUAL,2026-01-01,2026-01-31,1000,950,REC-01,0.25,kg,720,2,8,20"; // sin \n al final

        ResultadoLectura res = parser.parsear(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), "test.csv");

        assertThat(res.filas()).hasSize(1);
        assertThat(res.filas().get(0).numeroFila()).isEqualTo(3);
    }

    @Test
    @DisplayName("Parser CSV: encabezados con tildes normalizados")
    void encabezadosConTildes() throws IOException {
        String csv = "Producto Código,Planta Área,Frecuencia,Período Inicio,Período Fin,Demanda,Producción Real,"
                + "Recurso Código,Tiempo Procesamiento,Unidad Referencia,Tiempo Calendario,Número Turnos,Horas Por Turno,Días Operación\n"
                + "P1,Area,DIARIA,2026-01-01,2026-01-01,10,10,R1,1,kg,24,1,8,1";

        ResultadoLectura res = parser.parsear(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), "test.csv");

        assertThat(res.columnasFaltantes()).isEmpty();
        assertThat(res.filas()).hasSize(1);
    }

    @Test
    @DisplayName("Parser CSV: detecta columnas obligatorias faltantes")
    void columnasFaltantes() throws IOException {
        String csv = "producto_codigo,planta_area,frecuencia\nP1,Area,DIARIA";
        ResultadoLectura res = parser.parsear(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), "test.csv");

        assertThat(res.columnasFaltantes()).contains("periodo_inicio", "demanda", "recurso_codigo");
    }

    @Test
    @DisplayName("Parser CSV: archivo vacío")
    void archivoVacio() throws IOException {
        ResultadoLectura res = parser.parsear(new ByteArrayInputStream(new byte[0]), "test.csv");
        assertThat(res.filas()).isEmpty();
        assertThat(res.columnasFaltantes()).isNotEmpty();
    }

    @Test
    @DisplayName("Normalizador: normalizarNumero con comas, puntos y formatos latinos/anglosajones")
    void normalizarNumero() {
        assertThat(NormalizadorImportacion.normalizarNumero("1234,5")).isEqualByComparingTo("1234.5");
        assertThat(NormalizadorImportacion.normalizarNumero("1.234,5")).isEqualByComparingTo("1234.5");
        assertThat(NormalizadorImportacion.normalizarNumero("1,234.5")).isEqualByComparingTo("1234.5");
        assertThat(NormalizadorImportacion.normalizarNumero("1234.5")).isEqualByComparingTo("1234.5");
        assertThat(NormalizadorImportacion.normalizarNumero("  500  ")).isEqualByComparingTo("500");
    }
}
