package com.indux.induxbackend.infrastructure.adapter.in.web.controller;

import com.indux.induxbackend.application.port.in.ImportacionUseCase;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.model.ResultadoImportacion;
import com.indux.induxbackend.infrastructure.security.AccesoPorEmpresa;
import com.indux.induxbackend.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/importaciones")
@RequiredArgsConstructor
@Tag(name = "Importaciones", description = "Importación masiva de reportes desde archivos Excel (.xlsx) y CSV")
public class ImportacionController {

    private static final long MAX_BYTES = 5 * 1024 * 1024; // 5 MB

    private final ImportacionUseCase importacionUseCase;
    private final AccesoPorEmpresa acceso;

    @Operation(summary = "Importar reportes en vista previa o confirmación (Excel/CSV)")
    @PostMapping(value = "/reportes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultadoImportacion> importarReportes(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(name = "confirmar", defaultValue = "false") boolean confirmar,
            @RequestParam(name = "empresaId", required = false) Long empresaId,
            Authentication autenticacion) throws IOException {

        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        Long empresa = empresaId != null ? empresaId : usuario.empresaId();

        if (empresa == null) {
            throw new DominioException("Debes especificar el parámetro 'empresaId' para importar reportes");
        }

        acceso.verificarEmpresa(autenticacion, empresa);

        if (archivo == null || archivo.isEmpty()) {
            throw new DominioException("El archivo está vacío o no fue enviado");
        }

        if (archivo.getSize() > MAX_BYTES) {
            throw new DominioException("El archivo supera el tamaño máximo permitido de 5 MB");
        }

        ResultadoImportacion resultado = importacionUseCase.importar(
                archivo.getInputStream(),
                archivo.getOriginalFilename(),
                empresa,
                usuario.id(),
                confirmar);

        if (confirmar && !resultado.valido()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(resultado);
        }

        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Descargar plantilla de importación en formato Excel (.xlsx) o CSV")
    @GetMapping("/reportes/plantilla")
    public ResponseEntity<byte[]> descargarPlantilla(
            @RequestParam(name = "formato", defaultValue = "xlsx") String formato) {

        byte[] contenido = importacionUseCase.generarPlantilla(formato);
        boolean esCsv = "csv".equalsIgnoreCase(formato);

        String nombreArchivo = esCsv ? "plantilla_reportes.csv" : "plantilla_reportes.xlsx";
        MediaType mediaType = esCsv
                ? MediaType.parseMediaType("text/csv; charset=UTF-8")
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(mediaType)
                .body(contenido);
    }
}
