package ro.rentalplatform.rentalrequest.service;

import org.springframework.stereotype.Component;
import ro.rentalplatform.common.domain.StatusTransitionPolicy;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;

/**
 * Tranziții permise (CER-06):
 *   ACTIVE  ↔ FOUND
 *   ACTIVE  ↔ CLOSED
 * Orice altceva (inclusiv DELETED) este blocat.
 */
@Component
public class RequestStatusPolicy implements StatusTransitionPolicy<RequestStatus> {

    @Override
    public boolean isAllowed(RequestStatus from, RequestStatus to) {
        if (from == null || to == null || from == to) {
            return false;
        }
        if (from == RequestStatus.DELETED || to == RequestStatus.DELETED) {
            return false;
        }
        return switch (from) {
            case ACTIVE -> to == RequestStatus.FOUND || to == RequestStatus.CLOSED;
            case FOUND, CLOSED -> to == RequestStatus.ACTIVE;
            case DELETED -> false;
        };
    }
}
