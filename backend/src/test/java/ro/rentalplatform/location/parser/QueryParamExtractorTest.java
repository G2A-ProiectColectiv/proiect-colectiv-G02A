package ro.rentalplatform.location.parser;

import org.junit.jupiter.api.Test;
import ro.rentalplatform.location.domain.Coordinates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryParamExtractorTest {

    private final QueryParamExtractor extractor = new QueryParamExtractor();

    @Test
    void extractsFromQParameter() {
        Coordinates result = extractor.extract("https://maps.google.com/?q=44.4268,26.1025").orElseThrow();

        assertEquals(44.4268, result.latitude(), 1e-6);
        assertEquals(26.1025, result.longitude(), 1e-6);
    }

    @Test
    void extractsFromUrlEncodedQueryParameter() {
        // %2C is an encoded comma; it must be decoded before matching
        Coordinates result = extractor.extract("https://www.google.com/maps/search/?api=1&query=46.7712%2C23.6236")
                .orElseThrow();

        assertEquals(46.7712, result.latitude(), 1e-6);
        assertEquals(23.6236, result.longitude(), 1e-6);
    }

    @Test
    void extractsFromLlParameterAndLocPrefix() {
        Coordinates ll = extractor.extract("https://maps.google.com/maps?ll=45.7489,21.2087&z=14").orElseThrow();
        Coordinates loc = extractor.extract("https://maps.google.com/?q=loc:47.1585,27.6014").orElseThrow();

        assertEquals(45.7489, ll.latitude(), 1e-6);
        assertEquals(27.6014, loc.longitude(), 1e-6);
    }

    @Test
    void returnsEmptyForPlaceNameQuery() {
        assertTrue(extractor.extract("https://www.google.com/maps/search/?api=1&query=Piata+Unirii").isEmpty());
    }

    @Test
    void returnsEmptyWithoutSupportedParameter() {
        assertTrue(extractor.extract("https://www.google.com/maps/@44.4268,26.1025,15z").isEmpty());
        assertTrue(extractor.extract("https://example.com/?zoom=44.4268,26.1025").isEmpty());
        assertTrue(extractor.extract(null).isEmpty());
    }
}
