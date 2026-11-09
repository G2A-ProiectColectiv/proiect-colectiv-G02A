package ro.rentalplatform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email-ul este obligatoriu.")
        @Email(message = "Email-ul nu este valid.")
        String email,

        @NotBlank(message = "Parola este obligatorie.")
        @Size(min = 8, message = "Parola trebuie să aibă cel puțin 8 caractere.")
        String password,

        @NotBlank(message = "Prenumele este obligatoriu.")
        String firstName,

        @NotBlank(message = "Numele este obligatoriu.")
        String lastName,

        String phone,
        String avatarUrl,
        String bio,

        @NotBlank(message = "Rolul este obligatoriu.")
        String role
) {
}
