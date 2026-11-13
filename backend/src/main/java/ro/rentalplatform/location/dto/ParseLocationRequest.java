package ro.rentalplatform.location.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParseLocationRequest(
        @NotBlank(message = "Lipește linkul din Google Maps.")
        @Size(max = 2000, message = "Linkul poate avea cel mult 2000 de caractere.")
        String url
) {
}
