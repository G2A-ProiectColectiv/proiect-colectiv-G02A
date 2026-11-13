package ro.rentalplatform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank(message = "Token-ul este obligatoriu.")
        String token,

        @NotBlank(message = "Parola este obligatorie.")
        @Size(min = 8, message = "Parola trebuie să aibă cel puțin 8 caractere.")
        String newPassword
) {
}
