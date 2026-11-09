package ro.rentalplatform.user.dto;

public record UserDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String avatarUrl,
        String bio,
        String role,
        String status
) {
}
