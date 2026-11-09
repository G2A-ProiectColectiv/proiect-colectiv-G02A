package ro.rentalplatform.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceImplTest {

    @Test
    void shouldGenerateAndParseToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("this-is-a-test-secret-key-that-is-long-enough");
        properties.setExpiration(Duration.ofHours(1));

        JwtServiceImpl service = new JwtServiceImpl(properties);
        UserPrincipal principal = new UserPrincipal(42L, "admin@rental.local", "ADMIN");

        String token = service.generateToken(principal);
        UserPrincipal parsed = service.parseToken(token);

        assertThat(parsed).isEqualTo(principal);
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("this-is-a-test-secret-key-that-is-long-enough");
        properties.setExpiration(Duration.ofMillis(1));

        JwtServiceImpl service = new JwtServiceImpl(properties);
        UserPrincipal principal = new UserPrincipal(42L, "admin@rental.local", "ADMIN");

        String token = service.generateToken(principal);

        try {
            Thread.sleep(20L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> service.parseToken(token))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldRejectTamperedToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("this-is-a-test-secret-key-that-is-long-enough");
        properties.setExpiration(Duration.ofHours(1));

        JwtServiceImpl service = new JwtServiceImpl(properties);
        String token = service.generateToken(new UserPrincipal(42L, "admin@rental.local", "ADMIN"));
        String tampered = token.substring(0, token.length() - 1) + "A";

        assertThatThrownBy(() -> service.parseToken(tampered))
                .isInstanceOf(Exception.class);
    }
}
