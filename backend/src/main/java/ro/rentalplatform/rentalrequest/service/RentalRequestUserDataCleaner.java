package ro.rentalplatform.rentalrequest.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;
import ro.rentalplatform.rentalrequest.repository.RentalRequestRepository;
import ro.rentalplatform.user.service.UserDataCleaner;

/**
 * La ștergerea unui cont de chiriaș, toate cererile lui devin DELETED (STERG-02).
 * <p>
 * Spring îl înregistrează automat ca {@link UserDataCleaner} bean,
 * deci {@code AccountServiceImpl} îl primește fără nicio modificare.
 */
@Component
public class RentalRequestUserDataCleaner implements UserDataCleaner {

    private final RentalRequestRepository requestRepository;

    public RentalRequestUserDataCleaner(RentalRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    @Transactional
    public void cleanUp(Long userId) {
        requestRepository.markAllAsDeletedForTenant(userId, RequestStatus.DELETED);
    }
}
