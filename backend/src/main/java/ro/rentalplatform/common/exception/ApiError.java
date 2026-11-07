package ro.rentalplatform.common.exception;

import java.time.Instant;
import java.util.List;

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
