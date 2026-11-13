package ro.rentalplatform.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Prenumele este obligatoriu.")
        String firstName,

        @NotBlank(message = "Numele este obligatoriu.")
        String lastName,

        @Pattern(regexp = "^(07\\d{8}|\\+407\\d{8})$", message = "Introdu un număr valid (07XXXXXXXX sau +407XXXXXXXX).")
        String phone,

        @Size(max = 1000, message = "Descrierea poate avea maximum 1000 de caractere.")
        String bio
) {
}
