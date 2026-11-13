package ro.rentalplatform.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.rentalplatform.auth.domain.PasswordResetToken;
import ro.rentalplatform.user.domain.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    List<PasswordResetToken> findAllByUserAndUsedIsFalse(User user);
    void deleteAllByUser(User user);
    List<PasswordResetToken> findAllByUserAndExpiresAtBefore(User user, Instant now);
}
