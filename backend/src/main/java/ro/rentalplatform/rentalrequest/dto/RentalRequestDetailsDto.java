package ro.rentalplatform.rentalrequest.dto;

import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Detaliile unei cereri văzute de un proprietar (DET-05): preferințele, bugetul, perioada,
 * zona (centru + rază, dacă a fost aleasă) și datele chiriașului.
 * Câmpurile cererii au aceleași nume ca în {@link RentalRequestDto}.
 */
public record RentalRequestDetailsDto(
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
        Tenant tenant,
        Instant createdAt,
        Instant updatedAt
) {

    /** Chiriașul care a publicat cererea. Endpoint-ul cere autentificare, deci telefonul se trimite. */
    public record Tenant(
            Long id,
            String firstName,
            String lastName,
            String avatarUrl,
            String phone
    ) {
    }
}
