package ro.rentalplatform.common.exception;

import java.util.List;

/**
 * Cerere invalidă detectată în servicii (peste validările de pe DTO), întoarsă ca 400.
 */
public class BadRequestException extends RuntimeException {

    private final transient List<FieldErrorDto> fieldErrors;

    public BadRequestException(String message) {
        this(message, List.of());
    }

    public BadRequestException(String message, List<FieldErrorDto> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public List<FieldErrorDto> getFieldErrors() {
        return fieldErrors;
    }
}
