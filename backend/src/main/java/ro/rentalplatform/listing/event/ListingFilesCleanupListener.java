package ro.rentalplatform.listing.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ro.rentalplatform.storage.ImageStorageService;

@Component
public class ListingFilesCleanupListener {

    private final ImageStorageService imageStorageService;

    public ListingFilesCleanupListener(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onListingsDeleted(ListingsDeletedEvent event) {
        if (event == null || event.listingIds() == null) {
            return;
        }
        for (Long listingId : event.listingIds()) {
            if (listingId != null) {
                imageStorageService.deleteFolder("listings/" + listingId);
            }
        }
    }
}
