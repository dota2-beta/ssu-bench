package dev.backend.exception;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ошибки @Valid -> 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage()));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    // IllegalArgument, IllegalState -> 400
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<?> handleBusinessLogic(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Business logic error", ex.getMessage());
    }

    // авторизация BadCredentials-> 401
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<?> handleAuth(Exception ex) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed",
                "Invalid username or password"
        );
    }

    // RuntimeException и 404
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorType = "Runtime error";

        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
            status = HttpStatus.NOT_FOUND;
            errorType = "Resource not found";
        }

        return buildResponse(status, errorType, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleJsonErrors(HttpMessageNotReadableException ex) {
        String message = "Invalid JSON format or value";
        if (ex.getMessage() != null && ex.getMessage().contains("dev.backend.enums.Role")) {
            message = "Invalid role. Accepted values are: [CUSTOMER, EXECUTOR]";
        } else if (ex.getMessage() != null && ex.getMessage().contains("TaskStatus")) {
            message = "Invalid task status.";
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed JSON", message);
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobal(Exception ex) {
        ex.printStackTrace();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "Something went wrong");
    }

    private ResponseEntity<?> buildResponse(HttpStatus status, String error, Object message) {
        String requestId = MDC.get("requestId");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("request_id", requestId != null ? requestId : "n/a");
        body.put("error", error);
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}