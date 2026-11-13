package ro.rentalplatform.listing.event;

import java.util.List;

public record ListingsDeletedEvent(List<Long> listingIds) {
}
