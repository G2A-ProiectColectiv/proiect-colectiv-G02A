package ro.rentalplatform.security;

import java.util.Optional;

public interface CurrentUserProvider {
    Optional<Long> getCurrentUserId();

    Optional<String> getCurrentUserEmail();
}
