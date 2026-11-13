package ro.rentalplatform.listing.dto;

import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;
import ro.rentalplatform.listing.domain.Amenity;
import ro.rentalplatform.listing.domain.PropertyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Filtrele căutării publice de anunțuri (CAUT-01), citite din query string.
 * Orice filtru lipsă este ignorat.
 *
 * @param amenities  toate dotările din listă trebuie să existe la anunț
 *                   ({@code ?amenities=PARKING&amenities=BALCONY} sau {@code ?amenities=PARKING,BALCONY})
 * @param availableBy anunțul trebuie să fie disponibil cel târziu la această dată ({@code availableFrom <= availableBy})
 */
public record ListingSearchCriteria(
        @PositiveOrZero(message = "Prețul minim trebuie să fie 0 sau mai mare.")
        BigDecimal minPrice,

        @PositiveOrZero(message = "Prețul maxim trebuie să fie 0 sau mai mare.")
        BigDecimal maxPrice,

        PropertyType type,

        @PositiveOrZero(message = "Numărul minim de camere trebuie să fie 0 sau mai mare.")
        Short minRooms,

        @PositiveOrZero(message = "Numărul minim de băi trebuie să fie 0 sau mai mare.")
        Short minBathrooms,

        @PositiveOrZero(message = "Suprafața minimă trebuie să fie 0 sau mai mare.")
        BigDecimal minArea,

        @PositiveOrZero(message = "Suprafața maximă trebuie să fie 0 sau mai mare.")
        BigDecimal maxArea,

        String city,

        Set<Amenity> amenities,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate availableBy
) {
}
