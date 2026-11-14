package ro.rentalplatform.rentalrequest;

import org.junit.jupiter.api.Test;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;
import ro.rentalplatform.rentalrequest.service.RequestStatusPolicy;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestStatusPolicyTest {

    private final RequestStatusPolicy policy = new RequestStatusPolicy();

    @Test
    void allowsActiveToFoundAndClosedAndBack() {
        assertTrue(policy.isAllowed(RequestStatus.ACTIVE, RequestStatus.FOUND));
        assertTrue(policy.isAllowed(RequestStatus.FOUND, RequestStatus.ACTIVE));
        assertTrue(policy.isAllowed(RequestStatus.ACTIVE, RequestStatus.CLOSED));
        assertTrue(policy.isAllowed(RequestStatus.CLOSED, RequestStatus.ACTIVE));
    }

    @Test
    void foundToClosedIsNotAllowed() {
        // the doc's example: FOUND -> CLOSED must give 409
        assertFalse(policy.isAllowed(RequestStatus.FOUND, RequestStatus.CLOSED));
        assertFalse(policy.isAllowed(RequestStatus.CLOSED, RequestStatus.FOUND));
    }

    @Test
    void coversEveryCombinationOfStatuses() {
        // CER-06: exactly these four transitions are allowed, the other 12 pairs are not
        Set<String> allowed = Set.of(
                "ACTIVE->FOUND", "FOUND->ACTIVE",
                "ACTIVE->CLOSED", "CLOSED->ACTIVE");

        for (RequestStatus from : RequestStatus.values()) {
            for (RequestStatus to : RequestStatus.values()) {
                String pair = from + "->" + to;
                assertEquals(allowed.contains(pair), policy.isAllowed(from, to), pair);
            }
        }
    }

    @Test
    void rejectsNullStatuses() {
        assertFalse(policy.isAllowed(null, RequestStatus.ACTIVE));
        assertFalse(policy.isAllowed(RequestStatus.ACTIVE, null));
    }
}
