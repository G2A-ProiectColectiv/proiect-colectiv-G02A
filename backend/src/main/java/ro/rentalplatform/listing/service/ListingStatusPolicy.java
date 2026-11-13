package ro.rentalplatform.listing.service;

import org.springframework.stereotype.Component;
import ro.rentalplatform.common.domain.StatusTransitionPolicy;
import ro.rentalplatform.listing.domain.ListingStatus;

@Component
public class ListingStatusPolicy implements StatusTransitionPolicy<ListingStatus> {

    @Override
    public boolean isAllowed(ListingStatus from, ListingStatus to) {
        if (from == null || to == null || from == to) {
            return false;
        }
        if (from == ListingStatus.DELETED || to == ListingStatus.DELETED) {
            return false;
        }
        return switch (from) {
            case ACTIVE -> to == ListingStatus.RENTED || to == ListingStatus.UNAVAILABLE;
            case RENTED, UNAVAILABLE -> to == ListingStatus.ACTIVE;
            case DELETED -> false;
        };
    }
}
