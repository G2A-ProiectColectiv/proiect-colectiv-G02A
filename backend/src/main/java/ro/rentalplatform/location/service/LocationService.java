package ro.rentalplatform.location.service;

import ro.rentalplatform.location.domain.Coordinates;

public interface LocationService {
    Coordinates parse(String input);
}
