package ro.rentalplatform.location.parser;

import org.junit.jupiter.api.Test;
import ro.rentalplatform.location.domain.Coordinates;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleMapsLinkParserTest {

    // same order Spring builds from the @Order annotations
    private final GoogleMapsLinkParser parser = new GoogleMapsLinkParser(List.of(
            new PinDataExtractor(),
            new QueryParamExtractor(),
            new PathCoordinatesExtractor(),
            new ViewportExtractor(),
            new RawCoordinatesExtractor()
    ));

    @Test
    void prefersPinCoordinatesOverViewport() {
        String url = "https://www.google.com/maps/place/Bucuresti/@44.43,26.09,15z/data=!3m1!4b1!4m5!3m4!1s0x0:0x0!8m2!3d44.4267674!4d26.1025384";
        Optional<Coordinates> result = parser.parse(url);
        assertTrue(result.isPresent());
        assertEquals(44.4267674, result.get().latitude(), 1e-6);
        assertEquals(26.1025384, result.get().longitude(), 1e-6);
    }

    @Test
    void extractsQueryParamAndRawCoordinates() {
        Optional<Coordinates> queryResult = parser.parse("https://www.google.com/maps/search/?api=1&query=46.7712%2C23.6236");
        assertTrue(queryResult.isPresent());
        assertEquals(46.7712, queryResult.get().latitude(), 1e-4);
        assertEquals(23.6236, queryResult.get().longitude(), 1e-4);

        Optional<Coordinates> rawResult = parser.parse("44.4268, 26.1025");
        assertTrue(rawResult.isPresent());
        assertEquals(44.4268, rawResult.get().latitude(), 1e-4);
    }

    @Test
    void fallsBackToViewportWhenNothingMorePreciseExists() {
        Coordinates result = parser.parse("https://www.google.com/maps/@45.7489,21.2087,14z").orElseThrow();

        assertEquals(45.7489, result.latitude(), 1e-6);
        assertEquals(21.2087, result.longitude(), 1e-6);
    }

    @Test
    void trimsInputBeforeParsing() {
        assertTrue(parser.parse("   44.4268, 26.1025   ").isPresent());
    }

    @Test
    void rejectsDirectionsOrPlaceWithoutCoords() {
        assertTrue(parser.parse("https://www.google.com/maps/dir/44.42,26.10/44.45,26.12").isEmpty());
        assertTrue(parser.parse("https://www.google.com/maps/search/?api=1&query=Piata+Unirii").isEmpty());
        assertTrue(parser.parse("https://www.google.com/maps/place/Palatul+Parlamentului").isEmpty());
    }

    @Test
    void rejectsBlankInput() {
        assertTrue(parser.parse(null).isEmpty());
        assertTrue(parser.parse("  ").isEmpty());
    }
}
