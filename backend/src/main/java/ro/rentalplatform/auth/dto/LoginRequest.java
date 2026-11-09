package ro.rentalplatform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email-ul este obligatoriu.")
        @Email(message = "Email-ul nu este valid.")
        String email,

        @NotBlank(message = "Parola este obligatorie.")
        String password
) {
}
