package ro.rentalplatform.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtServiceImplTest {
    private static final String SECRET = "TestOnlySecretKeyThatIsAtLeast32BytesLong!!";

    private JwtServiceImpl jwtService;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        jwtService = createService(SECRET, Duration.ofHours(1));
        userPrincipal = new UserPrincipal(1L, "ana@test.ro", "TENANT");
    }

    private static JwtServiceImpl createService(String secret, Duration expiration) {
        JwtProperties props = new JwtProperties();
        props.setSecret(secret);
        props.setExpiration(expiration);
        return new JwtServiceImpl(props);
    }

    @Test
    void generatedTokenCanBeParsedBack() {
        String token = jwtService.generateToken(userPrincipal);

        UserPrincipal parsed = jwtService.parseToken(token);

        assertEquals(userPrincipal, parsed);
    }

    @Test
    void expiredTokenIsRejected() {
        JwtServiceImpl expiredTokenService = createService(SECRET, Duration.ofSeconds(-1));

        String token = expiredTokenService.generateToken(userPrincipal);

        assertThrows(ExpiredJwtException.class, () -> expiredTokenService.parseToken(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken(userPrincipal);

        String[] parts = token.split("\\.");

        String payload = parts[1];
        int i = payload.length() / 2;
        char original = payload.charAt(i);
        char replacement = (original == 'a') ? 'b' : 'a';
        parts[1] = payload.substring(0, i) + replacement + payload.substring(i + 1);

        String token2 = String.join(".", parts);

        assertThrows(JwtException.class, () -> jwtService.parseToken(token2));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtServiceImpl differentSecretService = createService("another_secret_that_has_more_than_32_characters_aaaaaaaa", Duration.ofHours(1));
        String token = differentSecretService.generateToken(userPrincipal);

        assertThrows(JwtException.class, () -> jwtService.parseToken(token));
    }
}
