package ro.rentalplatform.listing.dto;

public record ListingImageDto(
        Long id,
        String url,
        boolean primary,
        Short position
) {
}
