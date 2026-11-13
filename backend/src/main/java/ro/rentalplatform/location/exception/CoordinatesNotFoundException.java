package ro.rentalplatform.location.exception;

public class CoordinatesNotFoundException extends RuntimeException {

    public static final String DEFAULT_MESSAGE =
            "Nu am putut găsi coordonatele în acest link. "
            + "Pune un pin în Google Maps pe locuință și copiază linkul din nou.";

    public CoordinatesNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public CoordinatesNotFoundException(String message) {
        super(message);
    }
}
