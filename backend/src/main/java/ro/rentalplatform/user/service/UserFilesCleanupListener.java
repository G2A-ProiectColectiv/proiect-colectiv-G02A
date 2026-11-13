package ro.rentalplatform.user.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ro.rentalplatform.storage.ImageStorageService;
import ro.rentalplatform.user.event.UserDeletedEvent;

@Component
public class UserFilesCleanupListener {
    private final ImageStorageService imageStorageService;

    public UserFilesCleanupListener(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeleted(UserDeletedEvent event) {
        if (event.avatarPath() != null && !event.avatarPath().isBlank()) {
            imageStorageService.delete(event.avatarPath());
        }
    }
}
