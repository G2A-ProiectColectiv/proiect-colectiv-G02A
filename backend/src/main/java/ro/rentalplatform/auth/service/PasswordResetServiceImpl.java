package ro.rentalplatform.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.rentalplatform.auth.domain.PasswordResetToken;
import ro.rentalplatform.auth.repository.PasswordResetTokenRepository;
import ro.rentalplatform.common.exception.BadRequestException;
import ro.rentalplatform.notification.EmailService;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    private static final int TOKEN_LENGTH = 32;
    private static final Duration RESET_TOKEN_TTL = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String frontendUrl;

    public PasswordResetServiceImpl(UserRepository userRepository,
                                   PasswordResetTokenRepository passwordResetTokenRepository,
                                   PasswordEncoder passwordEncoder,
                                   EmailService emailService,
                                   @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (normalizedEmail.isBlank()) {
            return;
        }

        userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            invalidateExistingTokens(user);
            String rawToken = generateToken();
            String tokenHash = hashToken(rawToken);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenHash)
                    .expiresAt(Instant.now().plus(RESET_TOKEN_TTL))
                    .used(false)
                    .build();

            passwordResetTokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
            emailService.send(user.getEmail(), "Resetare parolă", "email/password-reset",
                    java.util.Map.of("resetLink", resetLink));
        });
    }

    @Override
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Token-ul este obligatoriu.");
        }

        String tokenHash = hashToken(token);
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Token-ul este invalid sau a expirat."));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Token-ul este invalid sau a expirat.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private void invalidateExistingTokens(User user) {
        List<PasswordResetToken> activeTokens = passwordResetTokenRepository.findAllByUserAndUsedIsFalse(user);
        activeTokens.forEach(token -> {
            token.setUsed(true);
            passwordResetTokenRepository.save(token);
        });
    }

    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        new SecureRandom().nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 nu este disponibil.", ex);
        }
    }
}
