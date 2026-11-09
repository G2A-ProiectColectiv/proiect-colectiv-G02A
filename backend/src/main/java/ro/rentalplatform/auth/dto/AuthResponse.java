package ro.rentalplatform.auth.dto;

import ro.rentalplatform.user.dto.UserDto;

public record AuthResponse(String token, UserDto user) {
}
