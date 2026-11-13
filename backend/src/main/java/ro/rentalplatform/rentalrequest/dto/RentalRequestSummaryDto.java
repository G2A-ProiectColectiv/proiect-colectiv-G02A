package ro.rentalplatform.rentalrequest.dto;

import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Versiunea compactă a cererii – folosită în listele paginate
 * (GET /api/requests/mine pentru chiriaș și GET /api/requests pentru proprietar).
 * Conține: titlu, buget, oraș, camere, perioadă, status, plus numele și poza chiriașului (CAUT-06).
 */
public record RentalRequestSummaryDto(
        Long id,
        String title,
        BigDecimal budgetMin,
        BigDecimal budgetMax,
        Currency currency,
        String city,
        String area,
        Short rooms,
        LocalDate desiredFrom,
        RequestStatus status,
        PropertyType desiredType,
        Tenant tenant,
        Instant createdAt
) {

    /** Datele publice ale chiriașului, afișate pe cardul cererii. */
    public record Tenant(
            Long id,
            String firstName,
            String lastName,
            String avatarUrl
    ) {
    }
}
