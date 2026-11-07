package ro.rentalplatform.common.exception;

public class BadCredentialsAppException extends RuntimeException {
    public BadCredentialsAppException(String message) {
        super(message);
    }
}
