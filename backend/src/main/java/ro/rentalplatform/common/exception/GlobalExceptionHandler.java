package ro.rentalplatform.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        List<FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldErrorDto(e.getField(), e.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Datele trimise nu sunt valide.", request, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex,
                                                     HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Corpul cererii este invalid.", request, null);
    }

    @ExceptionHandler(BadCredentialsAppException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsAppException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    @ExceptionHandler({ForbiddenOperationException.class, AccessDeniedException.class})
    public ResponseEntity<ApiError> handleForbidden(Exception ex, HttpServletRequest request) {
        String message = ex instanceof ForbiddenOperationException
                ? ex.getMessage()
                : "Nu ai dreptul să faci această operație.";
        return build(HttpStatus.FORBIDDEN, message, request, null);
    }

    @ExceptionHandler({NotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(Exception ex, HttpServletRequest request) {
        String message = ex instanceof NotFoundException
                ? ex.getMessage()
                : "Resursa nu a fost găsită.";
        return build(HttpStatus.NOT_FOUND, message, request, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest request) {
        log.error("Eroare neașteptată la {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "A apărut o eroare internă.", request, null);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           HttpServletRequest request, List<FieldErrorDto> fieldErrors) {
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), message, request.getRequestURI(), fieldErrors));
    }
}
