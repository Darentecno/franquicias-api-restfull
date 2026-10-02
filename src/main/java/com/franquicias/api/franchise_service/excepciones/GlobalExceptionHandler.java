package com.franquicias.api.franchise_service.excepciones;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.core.codec.DecodingException;
import com.franquicias.api.franchise_service.service.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler({DataIntegrityViolationException.class, WebExchangeBindException.class, DecodingException.class})
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(Exception ex) {
        String message = ex instanceof WebExchangeBindException bindException
                ? bindException.getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .findFirst().orElse("Solicitud inválida")
                : "La solicitud contiene datos inválidos o duplicados.";
        return buildResponse(HttpStatus.BAD_REQUEST, "Solicitud inválida", message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Unhandled exception while processing API request", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", "Ocurrió un error inesperado.");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String errorType, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now(Clock.systemUTC()));
        response.put("status", status.value());
        response.put("error", errorType);
        response.put("message", message);

        return new ResponseEntity<>(response, status);
    }
}