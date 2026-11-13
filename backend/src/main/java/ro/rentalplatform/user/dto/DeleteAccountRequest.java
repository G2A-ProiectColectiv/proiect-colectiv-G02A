package ro.rentalplatform.user.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(
        @NotBlank(message = "Parola este obligatorie.")
        String password
) {
}
