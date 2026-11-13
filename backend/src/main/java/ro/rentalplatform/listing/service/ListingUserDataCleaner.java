package ro.rentalplatform.listing.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ro.rentalplatform.listing.domain.Listing;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.event.ListingsDeletedEvent;
import ro.rentalplatform.listing.repository.ListingRepository;
import ro.rentalplatform.user.service.UserDataCleaner;

import java.util.List;

@Component
public class ListingUserDataCleaner implements UserDataCleaner {

    private final ListingRepository listingRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ListingUserDataCleaner(ListingRepository listingRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.listingRepository = listingRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void cleanUp(Long userId) {
        List<Listing> listings = listingRepository.findByLandlordIdAndStatusNot(userId, ListingStatus.DELETED);
        if (listings.isEmpty()) {
            return;
        }
        List<Long> deletedIds = listings.stream()
                .map(Listing::getId)
                .toList();
        for (Listing listing : listings) {
            listing.setStatus(ListingStatus.DELETED);
            listing.getImages().clear();
        }
        listingRepository.saveAll(listings);
        eventPublisher.publishEvent(new ListingsDeletedEvent(deletedIds));
    }
}
