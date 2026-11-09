package ro.rentalplatform.auth.service;

import ro.rentalplatform.auth.dto.AuthResponse;
import ro.rentalplatform.auth.dto.LoginRequest;
import ro.rentalplatform.auth.dto.RegisterRequest;
import ro.rentalplatform.user.dto.UserDto;

public interface AuthService {
    UserDto register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
