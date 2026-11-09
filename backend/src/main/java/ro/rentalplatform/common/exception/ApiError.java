package ro.rentalplatform.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/** Formatul unic de eroare al API-ului; {@code fieldErrors} apare doar la erorile de validare. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        int status,
        String message,
        String path,
        Instant timestamp,
        List<FieldErrorDto> fieldErrors
) {
    public static ApiError of(int status, String message, String path) {
        return new ApiError(status, message, path, Instant.now(), null);
    }

    public static ApiError of(int status, String message, String path, List<FieldErrorDto> fieldErrors) {
        return new ApiError(status, message, path, Instant.now(), fieldErrors);
    }
}
