package com.prediccion.apppredicciongm.gestion_inventario.inventario.errors;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice(basePackages = "com.prediccion.apppredicciongm.gestion_inventario.inventario")
@Slf4j
public class InventarioGlobalExceptionHandler {

        /**
         * Maneja excepciones de validación de datos (@Valid en DTOs)
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(
                        MethodArgumentNotValidException ex,
                        WebRequest request) {

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Validation Error")
                                .message("Error en la validación de los datos")
                                .details(errors)
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                log.error("Error de validación: {}", errors);
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        /**
         * Maneja violaciones de constraints (@NotNull, @Min, etc.)
         */
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolation(
                        ConstraintViolationException ex,
                        WebRequest request) {

                Map<String, String> errors = new HashMap<>();
                for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
                        String propertyPath = violation.getPropertyPath().toString();
                        String message = violation.getMessage();
                        errors.put(propertyPath, message);
                }

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Constraint Violation")
                                .message("Violación de restricciones de validación")
                                .details(errors)
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                log.error("Violación de constraints: {}", errors);
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        /**
         * Maneja argumentos ilegales (datos inválidos)
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
                        IllegalArgumentException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Bad Request")
                                .message(ex.getMessage())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                log.error("Argumento ilegal: {}", ex.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        /**
         * Maneja excepciones de estado ilegal (operación no permitida)
         */
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponse> handleIllegalStateException(
                        IllegalStateException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.CONFLICT.value())
                                .error("Conflict")
                                .message(ex.getMessage())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                log.error("Estado ilegal: {}", ex.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        /**
         * Maneja excepciones de stock insuficiente
         */
        @ExceptionHandler(StockInsuficienteException.class)
        public ResponseEntity<ErrorResponse> handleStockInsuficienteException(
                        StockInsuficienteException ex,
                        WebRequest request) {

                Map<String, Object> details = new HashMap<>();
                details.put("stockDisponible", ex.getStockDisponible());
                details.put("cantidadSolicitada", ex.getCantidadSolicitada());
                details.put("productoId", ex.getProductoId());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Stock Insuficiente")
                                .message(ex.getMessage())
                                .details(details)
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                log.error("Stock insuficiente: Producto {}, Disponible: {}, Solicitado: {}",
                                ex.getProductoId(), ex.getStockDisponible(), ex.getCantidadSolicitada());
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        /**
         * Maneja excepciones de inventario no encontrado
         */
        @ExceptionHandler(InventarioNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleInventarioNotFoundException(
                        InventarioNotFoundException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("Not Found")
                                .message(ex.getMessage())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                log.error("Inventario no encontrado: {}", ex.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        /**
         * Maneja excepciones de inventario ya existente
         */
        @ExceptionHandler(InventarioYaExisteException.class)
        public ResponseEntity<ErrorResponse> handleInventarioYaExisteException(
                        InventarioYaExisteException ex,
                        WebRequest request) {

                Map<String, Object> details = new HashMap<>();
                details.put("productoId", ex.getProductoId());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.CONFLICT.value())
                                .error("Conflict")
                                .message(ex.getMessage())
                                .details(details)
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                log.error("Inventario ya existe para el producto: {}", ex.getProductoId());
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        /**
         * Maneja cualquier otra excepción no contemplada
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(
                        Exception ex,
                        WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error("Internal Server Error")
                                .message("Ha ocurrido un error interno en el servidor")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                log.error("Error interno del servidor", ex);
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
