package ro.rentalplatform.listing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ro.rentalplatform.listing.domain.Listing;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.event.ListingFilesCleanupListener;
import ro.rentalplatform.listing.event.ListingsDeletedEvent;
import ro.rentalplatform.listing.repository.ListingRepository;
import ro.rentalplatform.listing.service.ListingUserDataCleaner;
import ro.rentalplatform.storage.ImageStorageService;
import ro.rentalplatform.user.domain.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static ro.rentalplatform.listing.ListingTestData.image;
import static ro.rentalplatform.listing.ListingTestData.landlord;
import static ro.rentalplatform.listing.ListingTestData.listing;

/** 1.3.11: a landlord's listings are deleted with the account, and their photo folders after commit. */
@ExtendWith(MockitoExtension.class)
class ListingUserDataCleanerTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ListingUserDataCleaner cleaner;

    @Test
    void marksAllListingsDeletedAndPublishesTheirIds() {
        User owner = landlord(1L);
        Listing active = listing(10L, owner, ListingStatus.ACTIVE);
        Listing rented = listing(11L, owner, ListingStatus.RENTED);
        active.getImages().add(image(100L, active, 0, true));
        when(listingRepository.findByLandlordIdAndStatusNot(1L, ListingStatus.DELETED))
                .thenReturn(List.of(active, rented));

        cleaner.cleanUp(1L);

        assertEquals(ListingStatus.DELETED, active.getStatus());
        assertEquals(ListingStatus.DELETED, rented.getStatus());
        assertTrue(active.getImages().isEmpty());
        verify(listingRepository).saveAll(List.of(active, rented));
        verify(eventPublisher).publishEvent(new ListingsDeletedEvent(List.of(10L, 11L)));
    }

    @Test
    void userWithoutListingsPublishesNothing() {
        when(listingRepository.findByLandlordIdAndStatusNot(1L, ListingStatus.DELETED)).thenReturn(List.of());

        cleaner.cleanUp(1L);

        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void listenerDeletesPhotoFolderOfEveryDeletedListing() {
        ImageStorageService storage = mock(ImageStorageService.class);
        ListingFilesCleanupListener listener = new ListingFilesCleanupListener(storage);

        listener.onListingsDeleted(new ListingsDeletedEvent(List.of(10L, 11L)));

        verify(storage).deleteFolder("listings/10");
        verify(storage).deleteFolder("listings/11");
    }

    @Test
    void listenerIgnoresEmptyEvent() {
        ImageStorageService storage = mock(ImageStorageService.class);
        ListingFilesCleanupListener listener = new ListingFilesCleanupListener(storage);

        listener.onListingsDeleted(new ListingsDeletedEvent(null));

        verifyNoInteractions(storage);
    }
}
