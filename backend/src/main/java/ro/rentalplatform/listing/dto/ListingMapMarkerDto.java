package ro.rentalplatform.listing.dto;

import ro.rentalplatform.listing.domain.Currency;

import java.math.BigDecimal;

/**
 * Un marker de pe harta de căutare (HARTA-02, HARTA-04): doar datele necesare pentru pin și popup.
 */
public record ListingMapMarkerDto(
        Long id,
        String title,
        BigDecimal price,
        Currency currency,
        String primaryImage,
        Double latitude,
        Double longitude
) {
}
