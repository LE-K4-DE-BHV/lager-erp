package com.lagermanagement.space.web.handler;

import com.lagermanagement.space.exception.BusinessException;
import com.lagermanagement.space.exception.EntityNotFoundException;
import com.lagermanagement.space.web.dto.ApiErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Zentraler Fehler-Handler: Fängt alle Exceptions ab und liefert einheitliches JSON.
 * Sicherheitsregel: Kein Stacktrace, keine DB-Interna, keine Tabellennamen ans Frontend.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> feldFehler = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Ungültiger Wert",
                        (first, second) -> first
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorDto.of(400, "Validierungsfehler", feldFehler));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorDto.of(404, ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDto> handleDataIntegrity(DataIntegrityViolationException ex) {
        // Interne DB-Details werden nicht weitergegeben
        log.warn("Datenbankkonflikt: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorDto.of(409, "Datenbankkonflikt: Ein Datensatz mit diesen Daten existiert bereits."));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorDto> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorDto.of(422, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleGeneric(Exception ex) {
        // Nur generische Meldung — kein Stacktrace, keine internen Details
        log.error("Unbehandelter Fehler: {}", ex.getClass().getSimpleName(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorDto.of(500, "Ein interner Fehler ist aufgetreten. Bitte kontaktiere den Administrator."));
    }
}
