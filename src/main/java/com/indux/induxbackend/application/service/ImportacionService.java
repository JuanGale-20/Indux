package com.indux.induxbackend.application.service;

import com.indux.induxbackend.application.port.in.ImportacionUseCase;
import com.indux.induxbackend.application.port.in.ReporteUseCase;
import com.indux.induxbackend.application.port.in.ReporteUseCase.CrearReporteComando;
import com.indux.induxbackend.application.port.in.ReporteUseCase.LineaComando;
import com.indux.induxbackend.application.port.out.ProductoRepositoryPort;
import com.indux.induxbackend.application.port.out.RecursoRepositoryPort;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.model.*;
import com.indux.induxbackend.domain.service.GeneradorPlantillaImportacion;
import com.indux.induxbackend.domain.service.NormalizadorImportacion;
import com.indux.induxbackend.domain.service.ParserArchivoImportacion;
import com.indux.induxbackend.domain.service.ParserArchivoImportacion.FilaLeida;
import com.indux.induxbackend.domain.service.ParserArchivoImportacion.ResultadoLectura;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportacionService implements ImportacionUseCase {

    private static final int MAX_FILAS = 5000;

    private final ProductoRepositoryPort productosRepo;
    private final RecursoRepositoryPort recursosRepo;
    private final ReporteUseCase reporteUseCase;

    private final ParserArchivoImportacion parser = new ParserArchivoImportacion();
    private final GeneradorPlantillaImportacion generadorPlantilla = new GeneradorPlantillaImportacion();

    @Override
    @Transactional
    public ResultadoImportacion importar(InputStream contenido, String nombreArchivo, Long empresaId,
                                         Long creadoPor, boolean confirmar) {
        if (empresaId == null) {
            throw new DominioException("La empresa es obligatoria para la importación");
        }

        ResultadoLectura lectura;
        try {
            lectura = parser.parsear(contenido, nombreArchivo);
        } catch (IOException e) {
            throw new DominioException("Error al leer el archivo de importación: " + e.getMessage());
        }

        if (!lectura.columnasFaltantes().isEmpty()) {
            throw new DominioException("Faltan columnas obligatorias: "
                    + String.join(", ", lectura.columnasFaltantes()));
        }

        List<FilaLeida> filas = lectura.filas();
        if (filas.size() > MAX_FILAS) {
            throw new DominioException("El archivo supera el límite de " + MAX_FILAS + " filas (contiene "
                    + filas.size() + ")");
        }

        // Cargar catálogo de la empresa en mapas case-insensitive
        Map<String, Producto> productosMap = new HashMap<>();
        for (Producto p : productosRepo.listarPorEmpresa(empresaId)) {
            productosMap.put(p.codigo().trim().toUpperCase(), p);
        }

        Map<String, Recurso> recursosMap = new HashMap<>();
        for (Recurso r : recursosRepo.listarPorEmpresa(empresaId)) {
            recursosMap.put(r.codigo().trim().toUpperCase(), r);
        }

        List<ErrorImportacion> errores = new ArrayList<>();

        record FilaProcesada(
                int numeroFila,
                String productoCodigo,
                Producto producto,
                String plantaArea,
                FrecuenciaReporte frecuencia,
                LocalDate periodoInicio,
                LocalDate periodoFin,
                BigDecimal demanda,
                BigDecimal produccionReal,
                String recursoCodigo,
                Recurso recurso,
                BigDecimal tiempoProc,
                String unidadRef,
                BigDecimal tiempoCal,
                BigDecimal tiempoNoDisp,
                int turnos,
                BigDecimal horasTurno,
                int dias) {}

        List<FilaProcesada> filasProcesadas = new ArrayList<>();

        for (FilaLeida f : filas) {
            int numFila = f.numeroFila();
            boolean filaValida = true;

            // Validar campos obligatorios no vacíos
            for (String col : ParserArchivoImportacion.COLUMNAS_OBLIGATORIAS) {
                String val = f.obtener(col);
                if (val == null || val.isBlank()) {
                    errores.add(new ErrorImportacion(numFila, col, "El campo '" + col + "' es obligatorio"));
                    filaValida = false;
                }
            }

            String prodCod = f.obtener("producto_codigo");
            Producto producto = null;
            if (prodCod != null && !prodCod.isBlank()) {
                producto = productosMap.get(prodCod.trim().toUpperCase());
                if (producto == null) {
                    errores.add(new ErrorImportacion(numFila, "producto_codigo",
                            "El producto con código '" + prodCod + "' no existe en la empresa"));
                    filaValida = false;
                }
            }

            String recCod = f.obtener("recurso_codigo");
            Recurso recurso = null;
            if (recCod != null && !recCod.isBlank()) {
                recurso = recursosMap.get(recCod.trim().toUpperCase());
                if (recurso == null) {
                    errores.add(new ErrorImportacion(numFila, "recurso_codigo",
                            "El recurso con código '" + recCod + "' no existe en la empresa"));
                    filaValida = false;
                }
            }

            String plantaArea = f.obtener("planta_area");

            FrecuenciaReporte frecuencia = null;
            String freqStr = f.obtener("frecuencia");
            if (freqStr != null && !freqStr.isBlank()) {
                try {
                    frecuencia = NormalizadorImportacion.parsearFrecuencia(freqStr);
                } catch (IllegalArgumentException e) {
                    errores.add(new ErrorImportacion(numFila, "frecuencia", e.getMessage()));
                    filaValida = false;
                }
            }

            LocalDate fechaInicio = null;
            String iniStr = f.obtener("periodo_inicio");
            if (iniStr != null && !iniStr.isBlank()) {
                try {
                    fechaInicio = NormalizadorImportacion.parsearFecha(iniStr);
                } catch (IllegalArgumentException e) {
                    errores.add(new ErrorImportacion(numFila, "periodo_inicio", e.getMessage()));
                    filaValida = false;
                }
            }

            LocalDate fechaFin = null;
            String finStr = f.obtener("periodo_fin");
            if (finStr != null && !finStr.isBlank()) {
                try {
                    fechaFin = NormalizadorImportacion.parsearFecha(finStr);
                } catch (IllegalArgumentException e) {
                    errores.add(new ErrorImportacion(numFila, "periodo_fin", e.getMessage()));
                    filaValida = false;
                }
            }

            if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
                errores.add(new ErrorImportacion(numFila, "periodo_fin",
                        "El fin del período no puede ser anterior al inicio"));
                filaValida = false;
            }

            BigDecimal demanda = parsearDecimal(f.obtener("demanda"), numFila, "demanda", false, errores);
            BigDecimal produccionReal = parsearDecimal(f.obtener("produccion_real"), numFila, "produccion_real", false, errores);
            BigDecimal tiempoProc = parsearDecimal(f.obtener("tiempo_procesamiento"), numFila, "tiempo_procesamiento", true, errores);
            String unidadRef = f.obtener("unidad_referencia");
            BigDecimal tiempoCal = parsearDecimal(f.obtener("tiempo_calendario"), numFila, "tiempo_calendario", false, errores);

            BigDecimal tiempoNoDisp = BigDecimal.ZERO;
            String noDispStr = f.obtener("tiempo_no_disponible");
            if (noDispStr != null && !noDispStr.isBlank()) {
                try {
                    tiempoNoDisp = NormalizadorImportacion.normalizarNumero(noDispStr);
                    if (tiempoNoDisp != null && tiempoNoDisp.signum() < 0) {
                        errores.add(new ErrorImportacion(numFila, "tiempo_no_disponible",
                                "El tiempo no disponible no puede ser negativo"));
                        filaValida = false;
                    }
                } catch (Exception e) {
                    errores.add(new ErrorImportacion(numFila, "tiempo_no_disponible",
                            "Número inválido '" + noDispStr + "'"));
                    filaValida = false;
                }
            }

            Integer turnos = parsearEntero(f.obtener("numero_turnos"), numFila, "numero_turnos", errores);
            BigDecimal horasTurno = parsearDecimal(f.obtener("horas_por_turno"), numFila, "horas_por_turno", true, errores);
            if (horasTurno != null && horasTurno.compareTo(BigDecimal.valueOf(24)) > 0) {
                errores.add(new ErrorImportacion(numFila, "horas_por_turno",
                        "Las horas por turno deben estar entre 0 y 24"));
                filaValida = false;
            }
            Integer dias = parsearEntero(f.obtener("dias_operacion"), numFila, "dias_operacion", errores);

            if (demanda == null || produccionReal == null || tiempoProc == null || tiempoCal == null
                    || turnos == null || horasTurno == null || dias == null) {
                filaValida = false;
            }

            if (filaValida) {
                // Validación con reglas de dominio
                try {
                    new Reporte(null, producto.id(), creadoPor, plantaArea, frecuencia,
                            fechaInicio, fechaFin, demanda, produccionReal, EstadoReporte.BORRADOR);
                    new ReporteRecurso(null, null, recurso.id(), tiempoProc, unidadRef,
                            tiempoCal, tiempoNoDisp, turnos, horasTurno, dias);
                } catch (DominioException ex) {
                    errores.add(new ErrorImportacion(numFila, "general", ex.getMessage()));
                    filaValida = false;
                }
            }

            if (filaValida) {
                filasProcesadas.add(new FilaProcesada(
                        numFila, prodCod.trim().toUpperCase(), producto, plantaArea, frecuencia,
                        fechaInicio, fechaFin, demanda, produccionReal, recCod.trim().toUpperCase(),
                        recurso, tiempoProc, unidadRef, tiempoCal, tiempoNoDisp, turnos, horasTurno, dias));
            }
        }

        // Agrupar filas procesadas por reporte
        record ClaveGrupoReporte(String productoCodigo, String plantaArea, FrecuenciaReporte frecuencia,
                                 LocalDate periodoInicio, LocalDate periodoFin) {}

        Map<ClaveGrupoReporte, List<FilaProcesada>> grupos = new LinkedHashMap<>();
        for (FilaProcesada fp : filasProcesadas) {
            ClaveGrupoReporte clave = new ClaveGrupoReporte(
                    fp.productoCodigo(), fp.plantaArea().trim().toLowerCase(), fp.frecuencia(),
                    fp.periodoInicio(), fp.periodoFin());
            grupos.computeIfAbsent(clave, k -> new ArrayList<>()).add(fp);
        }

        // Validaciones intra-grupo (demanda o producción distinta, recursos duplicados)
        for (Map.Entry<ClaveGrupoReporte, List<FilaProcesada>> entry : grupos.entrySet()) {
            List<FilaProcesada> grupoFilas = entry.getValue();
            FilaProcesada primera = grupoFilas.get(0);
            Set<String> recursosVistos = new HashSet<>();

            for (FilaProcesada actual : grupoFilas) {
                if (actual.demanda().compareTo(primera.demanda()) != 0) {
                    errores.add(new ErrorImportacion(actual.numeroFila(), "demanda",
                            "La demanda (" + actual.demanda() + ") es distinta a la informada en la fila "
                                    + primera.numeroFila() + " para este mismo reporte (" + primera.demanda() + ")"));
                }
                if (actual.produccionReal().compareTo(primera.produccionReal()) != 0) {
                    errores.add(new ErrorImportacion(actual.numeroFila(), "produccion_real",
                            "La producción real (" + actual.produccionReal() + ") es distinta a la informada en la fila "
                                    + primera.numeroFila() + " para este mismo reporte (" + primera.produccionReal() + ")"));
                }

                if (!recursosVistos.add(actual.recursoCodigo())) {
                    errores.add(new ErrorImportacion(actual.numeroFila(), "recurso_codigo",
                            "El recurso '" + actual.recursoCodigo() + "' está repetido en el reporte"));
                }
            }
        }

        boolean valido = errores.isEmpty();

        if (!confirmar) {
            // Vista previa
            List<ReporteImportadoResumen> resumenes = new ArrayList<>();
            for (Map.Entry<ClaveGrupoReporte, List<FilaProcesada>> entry : grupos.entrySet()) {
                FilaProcesada primera = entry.getValue().get(0);
                resumenes.add(new ReporteImportadoResumen(
                        primera.productoCodigo(), primera.plantaArea(), primera.frecuencia(),
                        primera.periodoInicio(), primera.periodoFin(), entry.getValue().size(), null));
            }
            return new ResultadoImportacion(valido, false, filas.size(), resumenes, errores);
        }

        // Confirmar = true
        if (!valido) {
            List<ReporteImportadoResumen> resumenes = new ArrayList<>();
            for (Map.Entry<ClaveGrupoReporte, List<FilaProcesada>> entry : grupos.entrySet()) {
                FilaProcesada primera = entry.getValue().get(0);
                resumenes.add(new ReporteImportadoResumen(
                        primera.productoCodigo(), primera.plantaArea(), primera.frecuencia(),
                        primera.periodoInicio(), primera.periodoFin(), entry.getValue().size(), null));
            }
            return new ResultadoImportacion(false, false, filas.size(), resumenes, errores);
        }

        // Crear reportes confirmados
        List<ReporteImportadoResumen> guardados = new ArrayList<>();
        for (Map.Entry<ClaveGrupoReporte, List<FilaProcesada>> entry : grupos.entrySet()) {
            List<FilaProcesada> grupoFilas = entry.getValue();
            FilaProcesada primera = grupoFilas.get(0);

            List<LineaComando> lineasComando = grupoFilas.stream().map(f -> new LineaComando(
                    f.recurso().id(), f.tiempoProc(), f.unidadRef(), f.tiempoCal(),
                    f.tiempoNoDisp(), f.turnos(), f.horasTurno(), f.dias())).toList();

            CrearReporteComando cmd = new CrearReporteComando(
                    primera.producto().id(), creadoPor, primera.plantaArea(), primera.frecuencia(),
                    primera.periodoInicio(), primera.periodoFin(), primera.demanda(),
                    primera.produccionReal(), lineasComando);

            ReporteUseCase.DetalleReporte creado = reporteUseCase.crear(cmd);

            guardados.add(new ReporteImportadoResumen(
                    primera.productoCodigo(), primera.plantaArea(), primera.frecuencia(),
                    primera.periodoInicio(), primera.periodoFin(), grupoFilas.size(), creado.reporte().id()));
        }

        return new ResultadoImportacion(true, true, filas.size(), guardados, List.of());
    }

    @Override
    public byte[] generarPlantilla(String formato) {
        if ("csv".equalsIgnoreCase(formato)) {
            return generadorPlantilla.generarCsv();
        }
        return generadorPlantilla.generarXlsx();
    }

    private static BigDecimal parsearDecimal(String texto, int fila, String col, boolean estrictoPositivo,
                                              List<ErrorImportacion> errores) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            BigDecimal val = NormalizadorImportacion.normalizarNumero(texto);
            if (val == null) {
                return null;
            }
            if (estrictoPositivo && val.signum() <= 0) {
                errores.add(new ErrorImportacion(fila, col, "El valor de '" + col + "' debe ser mayor que 0"));
                return null;
            }
            if (!estrictoPositivo && val.signum() < 0) {
                errores.add(new ErrorImportacion(fila, col, "El valor de '" + col + "' no puede ser negativo"));
                return null;
            }
            return val;
        } catch (Exception e) {
            errores.add(new ErrorImportacion(fila, col, "Número inválido '" + texto + "'"));
            return null;
        }
    }

    private static Integer parsearEntero(String texto, int fila, String col, List<ErrorImportacion> errores) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            int val = Integer.parseInt(texto.trim());
            if (val <= 0) {
                errores.add(new ErrorImportacion(fila, col, "El valor de '" + col + "' debe ser mayor que 0"));
                return null;
            }
            return val;
        } catch (Exception e) {
            errores.add(new ErrorImportacion(fila, col, "Entero inválido '" + texto + "'"));
            return null;
        }
    }
}
