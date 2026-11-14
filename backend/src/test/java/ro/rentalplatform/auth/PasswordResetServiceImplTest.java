package ro.rentalplatform.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.rentalplatform.auth.domain.PasswordResetToken;
import ro.rentalplatform.auth.repository.PasswordResetTokenRepository;
import ro.rentalplatform.auth.service.PasswordResetServiceImpl;
import ro.rentalplatform.common.exception.BadRequestException;
import ro.rentalplatform.notification.EmailService;
import ro.rentalplatform.user.domain.Role;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;
import ro.rentalplatform.user.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    private static final String FRONTEND_URL = "http://frontend.test";
    private static final String RAW_TOKEN = "raw-token-from-email";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<PasswordResetToken> tokenCaptor;
    @Captor
    private ArgumentCaptor<Map<String, Object>> emailVariablesCaptor;

    private PasswordResetServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        // built by hand because of the @Value frontend URL constructor parameter
        service = new PasswordResetServiceImpl(userRepository, tokenRepository, passwordEncoder,
                emailService, FRONTEND_URL);
        user = User.builder()
                .email("ana@test.ro")
                .passwordHash("old-hash")
                .firstName("Ana")
                .lastName("Pop")
                .role(Role.TENANT)
                .status(UserStatus.ACTIVE)
                .build();
    }

    /** Same hashing as the service: SHA-256, hex encoded. */
    private static String sha256(String value) throws NoSuchAlgorithmException {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    private PasswordResetToken storedToken(boolean used, Instant expiresAt) {
        return PasswordResetToken.builder()
                .user(user)
                .tokenHash("irrelevant")
                .used(used)
                .expiresAt(expiresAt)
                .build();
    }

    // ---- request ----

    @Test
    void requestForUnknownEmailDoesNothingButDoesNotFail() {
        when(userRepository.findByEmail("nobody@test.ro")).thenReturn(Optional.empty());

        // no exception, so the controller always answers 204
        service.requestPasswordReset("nobody@test.ro");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).send(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void requestWithBlankEmailDoesNothing() {
        service.requestPasswordReset("   ");

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void requestSavesOnlyTheHashAndEmailsTheRawToken() throws NoSuchAlgorithmException {
        when(userRepository.findByEmail("ana@test.ro")).thenReturn(Optional.of(user));
        when(tokenRepository.findAllByUserAndUsedIsFalse(user)).thenReturn(List.of());

        service.requestPasswordReset("  ANA@test.ro ");

        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken saved = tokenCaptor.getValue();

        verify(emailService).send(eq("ana@test.ro"), anyString(), eq("email/password-reset"),
                emailVariablesCaptor.capture());
        String link = (String) emailVariablesCaptor.getValue().get("resetLink");
        assertTrue(link.startsWith(FRONTEND_URL + "/reset-password?token="));
        String rawToken = link.substring(link.indexOf("token=") + "token=".length());

        // only the SHA-256 of the emailed token is stored, never the token itself
        assertNotEquals(rawToken, saved.getTokenHash());
        assertEquals(sha256(rawToken), saved.getTokenHash());
        assertFalse(saved.isUsed());
        assertEquals(user, saved.getUser());
    }

    @Test
    void requestCreatesTokenValidForOneHour() {
        when(userRepository.findByEmail("ana@test.ro")).thenReturn(Optional.of(user));
        when(tokenRepository.findAllByUserAndUsedIsFalse(user)).thenReturn(List.of());

        Instant before = Instant.now();
        service.requestPasswordReset("ana@test.ro");

        verify(tokenRepository).save(tokenCaptor.capture());
        Duration validity = Duration.between(before, tokenCaptor.getValue().getExpiresAt());
        assertTrue(validity.compareTo(Duration.ofMinutes(59)) > 0);
        assertTrue(validity.compareTo(Duration.ofMinutes(61)) < 0);
    }

    @Test
    void requestInvalidatesPreviousTokensOfTheSameUser() {
        PasswordResetToken oldToken = storedToken(false, Instant.now().plusSeconds(600));
        when(userRepository.findByEmail("ana@test.ro")).thenReturn(Optional.of(user));
        when(tokenRepository.findAllByUserAndUsedIsFalse(user)).thenReturn(List.of(oldToken));

        service.requestPasswordReset("ana@test.ro");

        assertTrue(oldToken.isUsed());
        verify(tokenRepository).save(oldToken);
    }

    // ---- confirm ----

    @Test
    void confirmWithValidTokenChangesPasswordAndMarksTokenUsed() throws NoSuchAlgorithmException {
        PasswordResetToken token = storedToken(false, Instant.now().plusSeconds(600));
        when(tokenRepository.findByTokenHash(sha256(RAW_TOKEN))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("parolaNoua123")).thenReturn("new-hash");

        service.confirmPasswordReset(RAW_TOKEN, "parolaNoua123");

        assertEquals("new-hash", user.getPasswordHash());
        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void confirmRejectsUnknownToken() throws NoSuchAlgorithmException {
        when(tokenRepository.findByTokenHash(sha256(RAW_TOKEN))).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.confirmPasswordReset(RAW_TOKEN, "parolaNoua123"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmRejectsAlreadyUsedToken() throws NoSuchAlgorithmException {
        PasswordResetToken token = storedToken(true, Instant.now().plusSeconds(600));
        when(tokenRepository.findByTokenHash(sha256(RAW_TOKEN))).thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class, () -> service.confirmPasswordReset(RAW_TOKEN, "parolaNoua123"));
        assertEquals("old-hash", user.getPasswordHash());
        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmRejectsExpiredToken() throws NoSuchAlgorithmException {
        PasswordResetToken token = storedToken(false, Instant.now().minusSeconds(1));
        when(tokenRepository.findByTokenHash(sha256(RAW_TOKEN))).thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class, () -> service.confirmPasswordReset(RAW_TOKEN, "parolaNoua123"));
        assertEquals("old-hash", user.getPasswordHash());
        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmRejectsBlankToken() {
        assertThrows(BadRequestException.class, () -> service.confirmPasswordReset(" ", "parolaNoua123"));
        verify(tokenRepository, never()).findByTokenHash(anyString());
    }
}
