package ro.rentalplatform.rentalrequest.dto;

import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Reprezentarea completă a unei cereri de închiriere (folosită la GET /api/requests/mine/{id}).
 */
public record RentalRequestDto(
        Long id,
        String title,
        String description,
        PropertyType desiredType,
        BigDecimal budgetMin,
        BigDecimal budgetMax,
        Currency currency,
        String city,
        String area,
        Short rooms,
        LocalDate desiredFrom,
        RequestStatus status,
        String googleMapsUrl,
        Double centerLatitude,
        Double centerLongitude,
        BigDecimal radiusKm,
        Instant createdAt,
        Instant updatedAt
) {
}
