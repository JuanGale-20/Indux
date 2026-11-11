package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.exception.DominioException;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ParserArchivoImportacion {

    public static final List<String> COLUMNAS_OBLIGATORIAS = List.of(
            "producto_codigo", "planta_area", "frecuencia", "periodo_inicio", "periodo_fin",
            "demanda", "produccion_real", "recurso_codigo", "tiempo_procesamiento",
            "unidad_referencia", "tiempo_calendario", "numero_turnos", "horas_por_turno", "dias_operacion"
    );

    public record FilaLeida(int numeroFila, Map<String, String> celdas) {
        public String obtener(String columna) {
            String val = celdas.get(columna);
            return val != null ? val.trim() : null;
        }
    }

    public record ResultadoLectura(
            List<String> encabezados,
            List<String> columnasFaltantes,
            List<FilaLeida> filas) {}

    public ResultadoLectura parsear(InputStream inputStream, String nombreArchivo) throws IOException {
        String nombre = nombreArchivo != null ? nombreArchivo.toLowerCase() : "";
        if (nombre.endsWith(".xlsx")) {
            return parsearXlsx(inputStream);
        } else {
            return parsearCsv(inputStream);
        }
    }

    private ResultadoLectura parsearCsv(InputStream inputStream) throws IOException {
        List<String> lineasRaw = leerLineasCsv(inputStream);
        if (lineasRaw.isEmpty()) {
            return new ResultadoLectura(List.of(), COLUMNAS_OBLIGATORIAS, List.of());
        }

        // Detectar encabezado y separador de la primera línea no vacía
        int indiceCabecera = -1;
        String lineaCabecera = null;
        for (int i = 0; i < lineasRaw.size(); i++) {
            String l = lineasRaw.get(i);
            if (l != null && !l.trim().isEmpty()) {
                indiceCabecera = i;
                lineaCabecera = l;
                break;
            }
        }

        if (lineaCabecera == null) {
            return new ResultadoLectura(List.of(), COLUMNAS_OBLIGATORIAS, List.of());
        }

        if (lineaCabecera.startsWith("\uFEFF")) {
            lineaCabecera = lineaCabecera.substring(1);
        }

        char separador = detectarSeparador(lineaCabecera);
        List<String> rawHeaders = dividirLineaCsv(lineaCabecera, separador);
        List<String> encabezados = rawHeaders.stream()
                .map(NormalizadorImportacion::normalizarEncabezado)
                .toList();

        List<String> faltantes = verificarColumnasFaltantes(encabezados);

        List<FilaLeida> filas = new ArrayList<>();
        for (int i = indiceCabecera + 1; i < lineasRaw.size(); i++) {
            String linea = lineasRaw.get(i);
            if (linea == null || linea.trim().isEmpty()) {
                continue;
            }
            int numeroFilaReal = i + 1; // 1-indexed
            List<String> valores = dividirLineaCsv(linea, separador);

            Map<String, String> celdas = new HashMap<>();
            boolean filaCompletamenteVacia = true;
            for (int col = 0; col < encabezados.size(); col++) {
                String enc = encabezados.get(col);
                String val = col < valores.size() ? valores.get(col).trim() : "";
                if (!val.isEmpty()) {
                    filaCompletamenteVacia = false;
                }
                celdas.put(enc, val);
            }

            if (!filaCompletamenteVacia) {
                filas.add(new FilaLeida(numeroFilaReal, celdas));
            }
        }

        return new ResultadoLectura(encabezados, faltantes, filas);
    }

    private ResultadoLectura parsearXlsx(InputStream inputStream) throws IOException {
        Workbook workbook;
        try {
            workbook = WorkbookFactory.create(inputStream);
        } catch (Exception e) {
            throw new DominioException("No se pudo leer el archivo Excel: formato no reconocido o corrupto");
        }

        if (workbook.getNumberOfSheets() == 0) {
            return new ResultadoLectura(List.of(), COLUMNAS_OBLIGATORIAS, List.of());
        }

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        Row cabeceraRow = null;
        while (rowIterator.hasNext()) {
            Row r = rowIterator.next();
            if (!esFilaVacia(r)) {
                cabeceraRow = r;
                break;
            }
        }

        if (cabeceraRow == null) {
            return new ResultadoLectura(List.of(), COLUMNAS_OBLIGATORIAS, List.of());
        }

        List<String> encabezados = new ArrayList<>();
        int lastCell = cabeceraRow.getLastCellNum();
        for (int c = 0; c < lastCell; c++) {
            Cell cell = cabeceraRow.getCell(c);
            String val = obtenerTextoCelda(cell);
            encabezados.add(NormalizadorImportacion.normalizarEncabezado(val));
        }

        List<String> faltantes = verificarColumnasFaltantes(encabezados);

        List<FilaLeida> filas = new ArrayList<>();
        while (rowIterator.hasNext()) {
            Row r = rowIterator.next();
            if (esFilaVacia(r)) {
                continue;
            }

            int numeroFilaReal = r.getRowNum() + 1;
            Map<String, String> celdas = new HashMap<>();
            boolean filaVacia = true;

            for (int col = 0; col < encabezados.size(); col++) {
                String enc = encabezados.get(col);
                Cell cell = r.getCell(col);
                String val = obtenerTextoCelda(cell);
                if (val != null && !val.trim().isEmpty()) {
                    filaVacia = false;
                }
                celdas.put(enc, val != null ? val.trim() : "");
            }

            if (!filaVacia) {
                filas.add(new FilaLeida(numeroFilaReal, celdas));
            }
        }

        return new ResultadoLectura(encabezados, faltantes, filas);
    }

    private static List<String> verificarColumnasFaltantes(List<String> encabezados) {
        Set<String> presentes = new HashSet<>(encabezados);
        List<String> faltantes = new ArrayList<>();
        for (String obligatoria : COLUMNAS_OBLIGATORIAS) {
            if (!presentes.contains(obligatoria)) {
                faltantes.add(obligatoria);
            }
        }
        return faltantes;
    }

    private static char detectarSeparador(String linea) {
        int comas = 0;
        int puntoYComas = 0;
        int tabs = 0;
        boolean enComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                enComillas = !enComillas;
            } else if (!enComillas) {
                if (c == ';') puntoYComas++;
                else if (c == ',') comas++;
                else if (c == '\t') tabs++;
            }
        }

        if (puntoYComas >= comas && puntoYComas >= tabs && puntoYComas > 0) {
            return ';';
        }
        if (tabs >= comas && tabs > puntoYComas && tabs > 0) {
            return '\t';
        }
        return ',';
    }

    private static List<String> dividirLineaCsv(String linea, char separador) {
        List<String> tokens = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean enComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                if (enComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"');
                    i++; // saltar comilla escapada
                } else {
                    enComillas = !enComillas;
                }
            } else if (c == separador && !enComillas) {
                tokens.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        tokens.add(actual.toString());
        return tokens;
    }

    private static List<String> leerLineasCsv(InputStream in) throws IOException {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder buffer = new StringBuilder();
            boolean enComillas = false;
            String line;
            while ((line = reader.readLine()) != null) {
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == '"') {
                        enComillas = !enComillas;
                    }
                }
                if (buffer.length() > 0) {
                    buffer.append('\n');
                }
                buffer.append(line);
                if (!enComillas) {
                    lineas.add(buffer.toString());
                    buffer.setLength(0);
                }
            }
            if (buffer.length() > 0) {
                lineas.add(buffer.toString());
            }
        }
        return lineas;
    }

    private static boolean esFilaVacia(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String s = obtenerTextoCelda(cell);
                if (s != null && !s.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String obtenerTextoCelda(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDate ld = cell.getDateCellValue().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    yield ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num) && !Double.isInfinite(num)) {
                    yield String.valueOf((long) num);
                }
                yield BigDecimal.valueOf(num).toPlainString();
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }
}
