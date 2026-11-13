package ro.rentalplatform.auth.service;

import org.springframework.stereotype.Service;
import ro.rentalplatform.auth.repository.PasswordResetTokenRepository;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.repository.UserRepository;
import ro.rentalplatform.user.service.UserDataCleaner;

import java.util.List;

@Service
public class PasswordResetTokenCleaner implements UserDataCleaner {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;

    public PasswordResetTokenCleaner(PasswordResetTokenRepository passwordResetTokenRepository,
                                    UserRepository userRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void cleanUp(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        List<ro.rentalplatform.auth.domain.PasswordResetToken> tokens =
                passwordResetTokenRepository.findAllByUserAndUsedIsFalse(user);
        tokens.forEach(token -> {
            token.setUsed(true);
            passwordResetTokenRepository.save(token);
        });
    }
}
