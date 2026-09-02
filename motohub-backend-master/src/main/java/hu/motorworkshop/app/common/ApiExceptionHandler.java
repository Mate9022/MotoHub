package hu.motorworkshop.app.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(
            NotFoundException exception
    ) {

        return Map.of(
                "timestamp", Instant.now(),
                "status", 404,
                "error", "Not Found",
                "message", exception.getMessage()
        );
    }


    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> errors =
                new LinkedHashMap<>();

        for (
                FieldError fieldError
                : exception
                .getBindingResult()
                .getFieldErrors()
        ) {

            errors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "timestamp",
                Instant.now()
        );

        response.put(
                "status",
                400
        );

        response.put(
                "error",
                "Validation Error"
        );

        response.put(
                "fields",
                errors
        );

        return response;
    }

    @ExceptionHandler(
            IllegalStateException.class
    )
    @ResponseStatus(
            HttpStatus.CONFLICT
    )
    public Map<String, Object> handleIllegalState(
            IllegalStateException exception
    ) {

        return Map.of(

                "timestamp",
                Instant.now(),

                "status",
                409,

                "error",
                "Conflict",

                "message",
                exception.getMessage()
        );
    }

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<?> handleIllegalArgument(
            IllegalArgumentException ex
    ) {

        return ResponseEntity
                .badRequest()
                .body(
                        Map.of(
                                "message",
                                ex.getMessage()
                        )
                );
    }
}