package ro.rentalplatform.location.parser;

import ro.rentalplatform.location.domain.Coordinates;

import java.util.Optional;

public interface CoordinateExtractor {
    Optional<Coordinates> extract(String input);
}
