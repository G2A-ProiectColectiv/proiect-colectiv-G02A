package ro.rentalplatform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey signingKey;
    private final JwtProperties jwtProperties;

    public JwtServiceImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpiration().toMillis());

        return Jwts.builder()
                .subject(String.valueOf(userPrincipal.id()))
                .claim("email", userPrincipal.email())
                .claim("role", userPrincipal.role())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public UserPrincipal parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new UserPrincipal(
                Long.parseLong(claims.getSubject()),
                claims.get("email", String.class),
                claims.get("role", String.class)
        );
    }
}
