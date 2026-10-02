package com.indux.induxbackend.domain.validacion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidadoresTest {

    private final ValidadorEmpresa empresa = new ValidadorEmpresa(CatalogosSteelForce.SECTORES);
    private final ValidadorProducto producto = new ValidadorProducto(CatalogosSteelForce.UNIDADES);
    private final ValidadorProceso proceso = new ValidadorProceso(CatalogosSteelForce.UNIDADES);

    // ---------- Empresa ----------

    @Test
    void empresaValidaNoTieneHallazgos() {
        ResultadoValidacion r = empresa.validar("Aceros del Caribe", "Manufactura");

        assertThat(r.estado()).isEqualTo(EstadoValidacion.VALIDO);
        assertThat(r.hallazgos()).isEmpty();
    }

    @Test
    void empresaSinNombreEsErrorCritico() {
        ResultadoValidacion r = empresa.validar("  ", "Manufactura");

        assertThat(r.estado()).isEqualTo(EstadoValidacion.CON_ERRORES_CRITICOS);
        assertThat(r.puedeAnalizarse()).isFalse();
        assertThat(r.erroresCriticos()).extracting(Hallazgo::variable)
                .containsExactly("Nombre de empresa");
    }

    @Test
    void sectorFueraDelCatalogoEsSoloAdvertencia() {
        ValidadorEmpresa conCatalogo = new ValidadorEmpresa(Catalogo.de("Metalmecánico", "Textil"));

        ResultadoValidacion r = conCatalogo.validar("Aceros", "Panadería");

        assertThat(r.estado()).isEqualTo(EstadoValidacion.CON_ADVERTENCIAS);
        assertThat(r.puedeAnalizarse()).isTrue();
        assertThat(r.advertencias()).extracting(Hallazgo::variable)
                .containsExactly("Sector manufacturero");
    }

    @Test
    void sectorDelCatalogoEsValidoSinImportarMayusculasNiEspacios() {
        ValidadorEmpresa conCatalogo = new ValidadorEmpresa(Catalogo.de("Metalmecánico", "Textil"));

        assertThat(conCatalogo.validar("Aceros", "  TEXTIL ").estado()).isEqualTo(EstadoValidacion.VALIDO);
    }

    @Test
    void sinCatalogoDeSectoresLaReglaNoSeAplica() {
        assertThat(empresa.validar("Aceros", "cualquier cosa").estado()).isEqualTo(EstadoValidacion.VALIDO);
    }

    // ---------- Producto ----------

    @Test
    void productoValidoNoTieneHallazgos() {
        assertThat(producto.validar("P-01", "Tornillo", "unidades").estado())
                .isEqualTo(EstadoValidacion.VALIDO);
    }

    @Test
    void productoReportaTodosLosErroresCriticosJuntos() {
        ResultadoValidacion r = producto.validar("", " ", null);

        assertThat(r.erroresCriticos()).extracting(Hallazgo::variable)
                .containsExactly("Código del producto", "Nombre del producto", "Unidad de medida");
    }

    @Test
    void unidadFueraDelCatalogoEsAdvertencia() {
        ResultadoValidacion r = producto.validar("P-01", "Aceite", "litros");

        assertThat(r.estado()).isEqualTo(EstadoValidacion.CON_ADVERTENCIAS);
        assertThat(r.puedeAnalizarse()).isTrue();
        assertThat(r.advertencias()).extracting(Hallazgo::variable).containsExactly("Unidad de medida");
    }

    @Test
    void unidadDelCatalogoEsValidaSinImportarMayusculas() {
        assertThat(producto.validar("P-01", "Acero", " KG ").estado()).isEqualTo(EstadoValidacion.VALIDO);
    }

    // ---------- Proceso ----------

    @Test
    void procesoValidoNoTieneHallazgos() {
        assertThat(proceso.validar("COR", "Corte", 1, "unidades").estado())
                .isEqualTo(EstadoValidacion.VALIDO);
    }

    @Test
    void procesoConSecuenciaCeroEsErrorCritico() {
        ResultadoValidacion r = proceso.validar("COR", "Corte", 0, "unidades");

        assertThat(r.erroresCriticos()).extracting(Hallazgo::variable)
                .containsExactly("Secuencia del proceso");
    }

    @Test
    void procesoReportaTodosLosErroresCriticosJuntos() {
        ResultadoValidacion r = proceso.validar(" ", "", 0, null);

        assertThat(r.erroresCriticos()).hasSize(4);
    }

    @Test
    void unidadProcesadaFueraDelCatalogoEsAdvertencia() {
        ResultadoValidacion r = proceso.validar("COR", "Corte", 1, "metros");

        assertThat(r.advertencias()).extracting(Hallazgo::variable).containsExactly("Unidad procesada");
    }

    // ---------- Resultado ----------

    @Test
    void combinarSumaLosHallazgosYConservaLaSeveridadMasAlta() {
        ResultadoValidacion advertencia = producto.validar("P-01", "Aceite", "litros");
        ResultadoValidacion error = proceso.validar("", "Corte", 1, "unidades");

        ResultadoValidacion combinado = advertencia.combinar(error);

        assertThat(combinado.hallazgos()).hasSize(2);
        assertThat(combinado.estado()).isEqualTo(EstadoValidacion.CON_ERRORES_CRITICOS);
    }
}
