package ro.rentalplatform.listing;

import org.junit.jupiter.api.Test;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.service.ListingStatusPolicy;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListingStatusPolicyTest {

    private final ListingStatusPolicy policy = new ListingStatusPolicy();

    @Test
    void allowsActiveToRentedAndUnavailableAndBack() {
        assertTrue(policy.isAllowed(ListingStatus.ACTIVE, ListingStatus.RENTED));
        assertTrue(policy.isAllowed(ListingStatus.ACTIVE, ListingStatus.UNAVAILABLE));
        assertTrue(policy.isAllowed(ListingStatus.RENTED, ListingStatus.ACTIVE));
        assertTrue(policy.isAllowed(ListingStatus.UNAVAILABLE, ListingStatus.ACTIVE));
    }

    @Test
    void rejectsDirectRentedUnavailableAndDeletedTransitions() {
        assertFalse(policy.isAllowed(ListingStatus.RENTED, ListingStatus.UNAVAILABLE));
        assertFalse(policy.isAllowed(ListingStatus.UNAVAILABLE, ListingStatus.RENTED));
        assertFalse(policy.isAllowed(ListingStatus.ACTIVE, ListingStatus.ACTIVE));
        assertFalse(policy.isAllowed(ListingStatus.ACTIVE, ListingStatus.DELETED));
        assertFalse(policy.isAllowed(ListingStatus.DELETED, ListingStatus.ACTIVE));
    }

    @Test
    void coversEveryCombinationOfStatuses() {
        // GEST-03: exactly these four transitions are allowed, everything else (16 - 4 = 12 pairs) is not
        Set<String> allowed = Set.of(
                "ACTIVE->RENTED", "RENTED->ACTIVE",
                "ACTIVE->UNAVAILABLE", "UNAVAILABLE->ACTIVE");

        for (ListingStatus from : ListingStatus.values()) {
            for (ListingStatus to : ListingStatus.values()) {
                String pair = from + "->" + to;
                assertEquals(allowed.contains(pair), policy.isAllowed(from, to), pair);
            }
        }
    }

    @Test
    void rejectsNullStatuses() {
        assertFalse(policy.isAllowed(null, ListingStatus.ACTIVE));
        assertFalse(policy.isAllowed(ListingStatus.ACTIVE, null));
    }
}
