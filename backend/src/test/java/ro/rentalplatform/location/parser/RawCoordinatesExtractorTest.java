package ro.rentalplatform.location.parser;

import org.junit.jupiter.api.Test;
import ro.rentalplatform.location.domain.Coordinates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RawCoordinatesExtractorTest {

    private final RawCoordinatesExtractor extractor = new RawCoordinatesExtractor();

    @Test
    void extractsCommaSeparatedPair() {
        // LOC-10: coordinates copied directly from Google Maps
        Coordinates result = extractor.extract("44.4268, 26.1025").orElseThrow();

        assertEquals(44.4268, result.latitude(), 1e-6);
        assertEquals(26.1025, result.longitude(), 1e-6);
    }

    @Test
    void acceptsParenthesesNoSpacesAndNegatives() {
        Coordinates withParentheses = extractor.extract("(44.4268,26.1025)").orElseThrow();
        Coordinates negative = extractor.extract("  -33.8568, 151.2153  ").orElseThrow();

        assertEquals(26.1025, withParentheses.longitude(), 1e-6);
        assertEquals(-33.8568, negative.latitude(), 1e-6);
    }

    @Test
    void returnsEmptyForOutOfRangeValues() {
        assertTrue(extractor.extract("91.0, 26.1").isEmpty());
        assertTrue(extractor.extract("44.4, 181.0").isEmpty());
    }

    @Test
    void returnsEmptyForAnythingElse() {
        assertTrue(extractor.extract("44.4268, 26.1025 Bucuresti").isEmpty());
        assertTrue(extractor.extract("https://www.google.com/maps/@44.4268,26.1025,15z").isEmpty());
        assertTrue(extractor.extract("Piata Unirii").isEmpty());
        assertTrue(extractor.extract("44.4268").isEmpty());
        assertTrue(extractor.extract(null).isEmpty());
    }
}
