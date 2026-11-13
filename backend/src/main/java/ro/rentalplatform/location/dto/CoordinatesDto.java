package ro.rentalplatform.location.dto;

import ro.rentalplatform.location.domain.Coordinates;

public record CoordinatesDto(double latitude, double longitude) {

    public static CoordinatesDto from(Coordinates coordinates) {
        return new CoordinatesDto(coordinates.latitude(), coordinates.longitude());
    }
}
