package ro.rentalplatform.listing;

import org.junit.jupiter.api.Test;
import ro.rentalplatform.common.exception.BadRequestException;
import ro.rentalplatform.listing.service.ListingSearchService.BoundingBox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoundingBoxTest {

    @Test
    void parsesSouthWestNorthEast() {
        BoundingBox box = BoundingBox.parse("44.33, 25.96, 44.55, 26.25");

        assertEquals(44.33, box.south());
        assertEquals(25.96, box.west());
        assertEquals(44.55, box.north());
        assertEquals(26.25, box.east());
    }

    @Test
    void westGreaterThanEastIsAllowedForTheAntimeridian() {
        BoundingBox box = BoundingBox.parse("-20,170,-10,-170");

        assertEquals(170, box.west());
        assertEquals(-170, box.east());
    }

    @Test
    void missingOrMalformedBboxIsRejected() {
        assertThrows(BadRequestException.class, () -> BoundingBox.parse(null));
        assertThrows(BadRequestException.class, () -> BoundingBox.parse("44.3,25.9,44.5"));
        assertThrows(BadRequestException.class, () -> BoundingBox.parse("44.3,25.9,abc,26.2"));
        assertThrows(BadRequestException.class, () -> BoundingBox.parse("44.3,25.9,NaN,26.2"));
    }

    @Test
    void outOfRangeOrInvertedLatitudesAreRejected() {
        assertThrows(BadRequestException.class, () -> BoundingBox.parse("-91,25.9,44.5,26.2"));
        assertThrows(BadRequestException.class, () -> BoundingBox.parse("44.3,-181,44.5,26.2"));
        assertThrows(BadRequestException.class, () -> BoundingBox.parse("44.5,25.9,44.3,26.2"));
    }

    @Test
    void errorIsReportedOnTheBboxParameter() {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> BoundingBox.parse(""));

        assertEquals("bbox", ex.getFieldErrors().get(0).field());
    }
}
