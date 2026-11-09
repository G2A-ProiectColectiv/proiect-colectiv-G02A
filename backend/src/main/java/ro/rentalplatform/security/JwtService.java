package ro.rentalplatform.security;

public interface JwtService {
    String generateToken(UserPrincipal userPrincipal);

    UserPrincipal parseToken(String token);
}
