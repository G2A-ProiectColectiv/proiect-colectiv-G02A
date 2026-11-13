package ro.rentalplatform.location.domain;

public record Coordinates(double latitude, double longitude) {

    public Coordinates {
        if (!Double.isFinite(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitudinea trebuie să fie între -90 și 90.");
        }
        if (!Double.isFinite(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitudinea trebuie să fie între -180 și 180.");
        }
    }

    public static boolean isValid(double latitude, double longitude) {
        return Double.isFinite(latitude)
                && Double.isFinite(longitude)
                && latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
    }
}
