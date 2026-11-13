package ro.rentalplatform.listing.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReorderImagesDto(
        @NotEmpty(message = "Lista cu ordinea pozelor nu poate fi goală.")
        List<Long> imageIds
) {
}
