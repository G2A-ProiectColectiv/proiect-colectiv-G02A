package ro.rentalplatform.listing.dto;

import jakarta.validation.constraints.NotNull;
import ro.rentalplatform.listing.domain.ListingStatus;

public record ChangeListingStatusDto(
        @NotNull(message = "Alege noul status.")
        ListingStatus status
) {
}
