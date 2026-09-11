package com.lagermanagement.space.web.handler;

import com.lagermanagement.space.exception.BusinessException;
import com.lagermanagement.space.exception.EntityNotFoundException;
import com.lagermanagement.space.web.dto.ApiErrorDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldReturn404WhenEntityNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("Artikel 42 nicht gefunden");

        ResponseEntity<ApiErrorDto> response = handler.handleEntityNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("Artikel 42 nicht gefunden");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void shouldReturn422WhenBusinessException() {
        BusinessException ex = new BusinessException("Bestand darf nicht negativ werden");

        ResponseEntity<ApiErrorDto> response = handler.handleBusiness(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(422);
        assertThat(response.getBody().message()).isEqualTo("Bestand darf nicht negativ werden");
    }

    @Test
    void shouldReturn409WhenDataIntegrityViolation() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("duplicate key value violates unique constraint artikelnummer");

        ResponseEntity<ApiErrorDto> response = handler.handleDataIntegrity(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        // Keine DB-internen Details in der Antwort
        assertThat(response.getBody().message()).doesNotContain("artikelnummer");
        assertThat(response.getBody().message()).doesNotContain("duplicate key");
    }

    @Test
    void shouldReturn500WithGenericMessageWhenUnhandledExceptionOccurs() {
        RuntimeException ex = new RuntimeException("Datenbankverbindung unterbrochen bei Tabelle artikel");

        ResponseEntity<ApiErrorDto> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        // Sicherheitscheck: Kein Stacktrace, keine internen Details
        assertThat(response.getBody().message()).doesNotContain("Datenbankverbindung");
        assertThat(response.getBody().message()).doesNotContain("artikel");
        assertThat(response.getBody().details()).isNull();
    }

    @Test
    void shouldReturn400WithFieldErrorsWhenValidationFails() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "bezeichnung", "darf nicht leer sein"));
        bindingResult.addError(new FieldError("request", "menge", "muss positiv sein"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiErrorDto> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);

        @SuppressWarnings("unchecked")
        var details = (java.util.Map<String, String>) response.getBody().details();
        assertThat(details).containsEntry("bezeichnung", "darf nicht leer sein");
        assertThat(details).containsEntry("menge", "muss positiv sein");
    }

    @Test
    void shouldContainTimestampInEveryErrorResponse() {
        ResponseEntity<ApiErrorDto> notFound =
                handler.handleEntityNotFound(new EntityNotFoundException("Test"));
        ResponseEntity<ApiErrorDto> business =
                handler.handleBusiness(new BusinessException("Test"));
        ResponseEntity<ApiErrorDto> generic =
                handler.handleGeneric(new RuntimeException("Test"));

        assertThat(notFound.getBody().timestamp()).isNotNull();
        assertThat(business.getBody().timestamp()).isNotNull();
        assertThat(generic.getBody().timestamp()).isNotNull();
    }
}
