package com.indux.induxbackend.domain.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GeneradorPlantillaImportacion {

    private static final String[] ENCABEZADOS = {
            "producto_codigo", "planta_area", "frecuencia", "periodo_inicio", "periodo_fin",
            "demanda", "produccion_real", "recurso_codigo", "tiempo_procesamiento",
            "unidad_referencia", "tiempo_calendario", "tiempo_no_disponible",
            "numero_turnos", "horas_por_turno", "dias_operacion"
    };

    private static final String[][] FILAS_EJEMPLO = {
            {"PROD-01", "Planta Principal", "MENSUAL", "2026-01-01", "2026-01-31",
                    "1000", "950", "REC-01", "0.25", "kg", "720", "0", "2", "8", "20"},
            {"PROD-01", "Planta Principal", "MENSUAL", "2026-01-01", "2026-01-31",
                    "1000", "950", "REC-02", "0.50", "kg", "720", "16", "2", "8", "20"}
    };

    public byte[] generarCsv() {
        StringBuilder sb = new StringBuilder();
        // BOM UTF-8
        sb.append('\uFEFF');
        sb.append(String.join(",", ENCABEZADOS)).append("\r\n");
        for (String[] fila : FILAS_EJEMPLO) {
            sb.append(String.join(",", fila)).append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generarXlsx() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Hoja 1: Reportes
            Sheet sheet1 = workbook.createSheet("Reportes");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row headerRow = sheet1.createRow(0);
            for (int i = 0; i < ENCABEZADOS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(ENCABEZADOS[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int r = 0; r < FILAS_EJEMPLO.length; r++) {
                Row row = sheet1.createRow(r + 1);
                for (int c = 0; c < FILAS_EJEMPLO[r].length; c++) {
                    row.createCell(c).setCellValue(FILAS_EJEMPLO[r][c]);
                }
            }

            for (int i = 0; i < ENCABEZADOS.length; i++) {
                sheet1.autoSizeColumn(i);
            }

            // Hoja 2: Instrucciones
            Sheet sheet2 = workbook.createSheet("Instrucciones");
            String[] instrucciones = {
                    "INSTRUCCIONES DE IMPORTACIÓN DE REPORTES - INDUX PLATAFORMA",
                    "",
                    "1. Cada fila representa un recurso evaluado en un reporte.",
                    "2. Las filas que compartan (producto_codigo, planta_area, frecuencia, periodo_inicio, periodo_fin)",
                    "   se agruparán automáticamente en un mismo reporte.",
                    "3. Para un mismo reporte, los campos 'demanda' y 'produccion_real' deben coincidir en todas sus filas.",
                    "4. Formato de fechas admitido: AAAA-MM-DD (ej. 2026-01-01) o DD/MM/AAAA (ej. 01/01/2026).",
                    "5. Formato de números: punto o coma decimal (ej. 1234.50 o 1234,50).",
                    "6. Frecuencias permitidas: DIARIA, SEMANAL, MENSUAL, ANUAL (sin distinguir mayúsculas/minúsculas).",
                    "7. El campo 'tiempo_no_disponible' es opcional (puede dejarse vacío o en 0).",
                    "8. Las columnas producto_codigo y recurso_codigo deben existir previamente registrados en la empresa."
            };

            for (int i = 0; i < instrucciones.length; i++) {
                Row row = sheet2.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellValue(instrucciones[i]);
                if (i == 0) {
                    cell.setCellStyle(headerStyle);
                }
            }
            sheet2.autoSizeColumn(0);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar archivo Excel de plantilla", e);
        }
    }
}
