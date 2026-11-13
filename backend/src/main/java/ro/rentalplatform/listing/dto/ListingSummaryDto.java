package ro.rentalplatform.listing.dto;

import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.domain.PropertyType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ListingSummaryDto(
        Long id,
        String title,
        BigDecimal price,
        Currency currency,
        ListingStatus status,
        String primaryImage,
        Instant createdAt,
        PropertyType type,
        Short rooms,
        Short bathrooms,
        BigDecimal areaSqm,
        String city,
        LocalDate availableFrom
) {
}
