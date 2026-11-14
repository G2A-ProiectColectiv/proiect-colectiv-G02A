package ro.rentalplatform.location.parser;

import org.junit.jupiter.api.Test;
import ro.rentalplatform.location.domain.Coordinates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathCoordinatesExtractorTest {

    private final PathCoordinatesExtractor extractor = new PathCoordinatesExtractor();

    @Test
    void extractsFromPlacePath() {
        Coordinates result = extractor.extract("https://www.google.com/maps/place/44.4268,26.1025").orElseThrow();

        assertEquals(44.4268, result.latitude(), 1e-6);
        assertEquals(26.1025, result.longitude(), 1e-6);
    }

    @Test
    void extractsFromSearchPathWithEncodedSeparator() {
        Coordinates result = extractor.extract("https://www.google.com/maps/search/44.4268,+26.1025?entry=ttu")
                .orElseThrow();

        assertEquals(44.4268, result.latitude(), 1e-6);
        assertEquals(26.1025, result.longitude(), 1e-6);
    }

    @Test
    void extractsNegativeCoordinates() {
        Coordinates result = extractor.extract("https://www.google.com/maps/place/-33.8568,151.2153/").orElseThrow();

        assertEquals(-33.8568, result.latitude(), 1e-6);
        assertEquals(151.2153, result.longitude(), 1e-6);
    }

    @Test
    void returnsEmptyForPlaceNameOrDirections() {
        assertTrue(extractor.extract("https://www.google.com/maps/place/Palatul+Parlamentului").isEmpty());
        assertTrue(extractor.extract("https://www.google.com/maps/dir/44.42,26.10/44.45,26.12").isEmpty());
        assertTrue(extractor.extract(null).isEmpty());
    }
}
