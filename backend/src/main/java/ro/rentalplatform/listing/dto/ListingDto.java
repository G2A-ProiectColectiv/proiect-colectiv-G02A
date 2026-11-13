package ro.rentalplatform.listing.dto;

import ro.rentalplatform.listing.domain.Amenity;
import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.domain.PropertyType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ListingDto(
        Long id,
        String title,
        String description,
        PropertyType type,
        BigDecimal price,
        Currency currency,
        Short rooms,
        Short bathrooms,
        BigDecimal areaSqm,
        String address,
        String city,
        List<Amenity> amenities,
        LocalDate availableFrom,
        String googleMapsUrl,
        Double latitude,
        Double longitude,
        ListingStatus status,
        List<ListingImageDto> images,
        Instant createdAt,
        Instant updatedAt
) {
}
