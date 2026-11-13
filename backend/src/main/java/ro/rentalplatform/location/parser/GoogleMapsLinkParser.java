package ro.rentalplatform.location.parser;

import org.springframework.stereotype.Component;
import ro.rentalplatform.location.domain.Coordinates;

import java.util.List;
import java.util.Optional;

@Component
public class GoogleMapsLinkParser {

    private final List<CoordinateExtractor> extractors;

    public GoogleMapsLinkParser(List<CoordinateExtractor> extractors) {
        this.extractors = extractors;
    }

    public Optional<Coordinates> parse(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        String trimmed = input.trim();
        if (trimmed.contains("/maps/dir/") || trimmed.contains("/dir/")) {
            return Optional.empty();
        }
        for (CoordinateExtractor extractor : extractors) {
            Optional<Coordinates> result = extractor.extract(trimmed);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
}
