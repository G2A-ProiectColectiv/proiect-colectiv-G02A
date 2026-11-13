package ro.rentalplatform.rentalrequest.dto;

import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Versiunea compactă a cererii – folosită în lista paginată (GET /api/requests/mine).
 * Conține: titlu, buget, oraș, camere, perioadă, status.
 */
public record RentalRequestSummaryDto(
        Long id,
        String title,
        BigDecimal budgetMin,
        BigDecimal budgetMax,
        Currency currency,
        String city,
        Short rooms,
        LocalDate desiredFrom,
        RequestStatus status,
        PropertyType desiredType,
        Instant createdAt
) {
}
