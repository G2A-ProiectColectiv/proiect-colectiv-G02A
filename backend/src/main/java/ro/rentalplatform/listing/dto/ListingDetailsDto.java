package ro.rentalplatform.listing.dto;

import ro.rentalplatform.listing.domain.Amenity;
import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.domain.PropertyType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Pagina publică a unui anunț (DET-01): tot anunțul, pozele și proprietarul.
 * Telefonul proprietarului este {@code null} pentru vizitatorii neautentificați (DET-03).
 */
public record ListingDetailsDto(
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
        Owner owner,
        Instant createdAt,
        Instant updatedAt
) {

    /** Datele publice ale proprietarului. {@code phone} apare doar pentru utilizatorii autentificați. */
    public record Owner(
            Long id,
            String firstName,
            String lastName,
            String avatarUrl,
            String phone
    ) {
    }
}
