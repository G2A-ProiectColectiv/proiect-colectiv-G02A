package ro.rentalplatform.location.parser;

import org.junit.jupiter.api.Test;
import ro.rentalplatform.location.domain.Coordinates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PinDataExtractorTest {

    private final PinDataExtractor extractor = new PinDataExtractor();

    @Test
    void extractsPinFromPlaceLink() {
        String url = "https://www.google.com/maps/place/Palatul+Parlamentului/@44.4275,26.0875,17z/"
                + "data=!3m1!4b1!4m6!3m5!1s0x40b1ff4f4d3d0f0d:0x4f1c!8m2!3d44.4275035!4d26.0873213!16s%2Fm%2F01k5wj";

        Coordinates result = extractor.extract(url).orElseThrow();

        assertEquals(44.4275035, result.latitude(), 1e-7);
        assertEquals(26.0873213, result.longitude(), 1e-7);
    }

    @Test
    void acceptsNegativeValuesAndManyDecimals() {
        String url = "https://www.google.com/maps/place/Sydney+Opera+House/data=!4m2!3m1!1s0x0:0x0!8m2"
                + "!3d-33.8567844!4d151.2152967";

        Coordinates result = extractor.extract(url).orElseThrow();

        assertEquals(-33.8567844, result.latitude(), 1e-7);
        assertEquals(151.2152967, result.longitude(), 1e-7);
    }

    @Test
    void returnsEmptyWhenThereIsNoPinData() {
        assertTrue(extractor.extract("https://www.google.com/maps/@44.4268,26.1025,15z").isEmpty());
        assertTrue(extractor.extract("nu este un link").isEmpty());
        assertTrue(extractor.extract("").isEmpty());
        assertTrue(extractor.extract(null).isEmpty());
    }

    @Test
    void ignoresOutOfRangeCoordinates() {
        assertTrue(extractor.extract("https://www.google.com/maps/data=!3d95.0!4d26.1").isEmpty());
    }
}
