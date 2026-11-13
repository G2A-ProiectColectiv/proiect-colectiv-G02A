package ro.rentalplatform.location;

import org.junit.jupiter.api.Test;
import ro.rentalplatform.location.domain.Coordinates;
import ro.rentalplatform.location.parser.GoogleMapsLinkParser;
import ro.rentalplatform.location.parser.PathCoordinatesExtractor;
import ro.rentalplatform.location.parser.PinDataExtractor;
import ro.rentalplatform.location.parser.QueryParamExtractor;
import ro.rentalplatform.location.parser.RawCoordinatesExtractor;
import ro.rentalplatform.location.parser.ViewportExtractor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleMapsLinkParserTest {

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
    void rejectsDirectionsOrPlaceWithoutCoords() {
        assertTrue(parser.parse("https://www.google.com/maps/dir/44.42,26.10/44.45,26.12").isEmpty());
        assertTrue(parser.parse("https://www.google.com/maps/search/?api=1&query=Piata+Unirii").isEmpty());
    }
}
