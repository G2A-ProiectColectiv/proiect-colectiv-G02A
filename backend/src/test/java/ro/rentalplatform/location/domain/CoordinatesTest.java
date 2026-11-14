package ro.rentalplatform.location.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** LOC-08: coordinates outside the valid ranges are rejected. */
class CoordinatesTest {

    @Test
    void acceptsBoundaryValues() {
        assertDoesNotThrow(() -> new Coordinates(90, 180));
        assertDoesNotThrow(() -> new Coordinates(-90, -180));
    }

    @Test
    void rejectsOutOfRangeAndNonFiniteValues() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(90.0001, 0));
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(0, -180.0001));
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(0, Double.POSITIVE_INFINITY));
    }

    @Test
    void isValidMatchesConstructorRules() {
        assertTrue(Coordinates.isValid(44.4268, 26.1025));
        assertFalse(Coordinates.isValid(-91, 0));
        assertFalse(Coordinates.isValid(0, Double.NaN));
    }
}
