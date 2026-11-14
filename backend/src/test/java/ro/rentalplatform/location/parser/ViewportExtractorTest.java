package ro.rentalplatform.location.parser;

import org.junit.jupiter.api.Test;
import ro.rentalplatform.location.domain.Coordinates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewportExtractorTest {

    private final ViewportExtractor extractor = new ViewportExtractor();

    @Test
    void extractsMapCenterWithZoom() {
        Coordinates result = extractor.extract("https://www.google.com/maps/@44.4267674,26.1025384,15z").orElseThrow();

        assertEquals(44.4267674, result.latitude(), 1e-7);
        assertEquals(26.1025384, result.longitude(), 1e-7);
    }

    @Test
    void extractsCenterFromPlaceLinkAndSatelliteView() {
        Coordinates place = extractor.extract(
                "https://www.google.com/maps/place/Ateneul+Roman/@44.4413,26.0973,17z/data=!3m1").orElseThrow();
        Coordinates satellite = extractor.extract(
                "https://www.google.com/maps/@46.7712,23.6236,250m/data=!3m1!1e3").orElseThrow();

        assertEquals(44.4413, place.latitude(), 1e-6);
        assertEquals(23.6236, satellite.longitude(), 1e-6);
    }

    @Test
    void returnsEmptyForDirectionsAndLinksWithoutViewport() {
        assertTrue(extractor.extract("https://www.google.com/maps/dir/Bucuresti/Cluj/@45.5,24.9,8z").isEmpty());
        assertTrue(extractor.extract("https://www.google.com/maps/place/Palatul+Parlamentului").isEmpty());
        assertTrue(extractor.extract(null).isEmpty());
    }
}
