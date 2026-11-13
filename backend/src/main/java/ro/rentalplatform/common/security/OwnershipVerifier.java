package ro.rentalplatform.common.security;

import org.springframework.stereotype.Component;
import ro.rentalplatform.common.domain.OwnedResource;
import ro.rentalplatform.common.exception.ForbiddenOperationException;
import ro.rentalplatform.common.exception.NotFoundException;

import java.util.Objects;

@Component
public class OwnershipVerifier {

    public void verify(OwnedResource resource, Long currentUserId) {
        if (resource == null) {
            throw new NotFoundException("Resursa nu a fost găsită.");
        }
        if (currentUserId == null || !Objects.equals(resource.getOwnerId(), currentUserId)) {
            throw new ForbiddenOperationException("Nu ai dreptul să modifici această resursă.");
        }
    }
}
