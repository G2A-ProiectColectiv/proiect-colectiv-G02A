package ro.rentalplatform.auth.service;

import ro.rentalplatform.auth.dto.AuthResponse;
import ro.rentalplatform.auth.dto.LoginRequest;
import ro.rentalplatform.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
