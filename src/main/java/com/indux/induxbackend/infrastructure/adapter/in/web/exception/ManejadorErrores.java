package com.indux.induxbackend.infrastructure.adapter.in.web.exception;

import com.indux.induxbackend.domain.exception.ConflictoException;
import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.exception.NoEncontradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.indux.induxbackend.domain.exception.CredencialesInvalidasException;
import com.indux.induxbackend.domain.exception.SinPermisoException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<ProblemDetail> noEncontrado(NoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ProblemDetail> conflicto(ConflictoException ex) {
        return respuesta(HttpStatus.CONFLICT, "Conflicto con los datos existentes", ex.getMessage());
    }

    @ExceptionHandler(DominioException.class)
    public ResponseEntity<ProblemDetail> reglaDeDominio(DominioException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Regla de negocio incumplida", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Hay campos inválidos o faltantes");
        problema.setTitle("Datos inválidos");
        problema.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(problema);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> jsonIlegible(HttpMessageNotReadableException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Cuerpo ilegible", "El JSON enviado no tiene un formato válido");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> inesperado(Exception ex) {
        log.error("Error inesperado", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado");
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ProblemDetail> credencialesInvalidas(CredencialesInvalidasException ex) {
        return respuesta(HttpStatus.UNAUTHORIZED, "No autenticado", ex.getMessage());
    }

    @ExceptionHandler(SinPermisoException.class)
    public ResponseEntity<ProblemDetail> sinPermiso(SinPermisoException ex) {
        return respuesta(HttpStatus.FORBIDDEN, "Sin permiso", ex.getMessage());
    }

    private ResponseEntity<ProblemDetail> respuesta(HttpStatus estado, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        return ResponseEntity.status(estado).body(problema);
    }
}
