package ro.rentalplatform.listing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ro.rentalplatform.listing.domain.Amenity;
import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.PropertyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record CreateListingDto(
        @NotBlank(message = "Introdu titlul.")
        @Size(max = 150, message = "Titlul poate avea cel mult 150 de caractere.")
        String title,

        @NotBlank(message = "Scrie o descriere.")
        String description,

        @NotNull(message = "Alege tipul locuinței.")
        PropertyType type,

        @NotNull(message = "Introdu prețul.")
        @Positive(message = "Prețul trebuie să fie mai mare decât 0.")
        BigDecimal price,

        @NotNull(message = "Alege moneda.")
        Currency currency,

        @NotNull(message = "Introdu numărul de camere.")
        @PositiveOrZero(message = "Numărul de camere trebuie să fie 0 sau mai mare.")
        Short rooms,

        @NotNull(message = "Introdu numărul de băi.")
        @PositiveOrZero(message = "Numărul de băi trebuie să fie 0 sau mai mare.")
        Short bathrooms,

        @NotNull(message = "Introdu suprafața.")
        @Positive(message = "Suprafața trebuie să fie mai mare decât 0.")
        BigDecimal areaSqm,

        @NotBlank(message = "Introdu adresa.")
        @Size(max = 255, message = "Adresa poate avea cel mult 255 de caractere.")
        String address,

        @NotBlank(message = "Introdu orașul.")
        @Size(max = 100, message = "Orașul poate avea cel mult 100 de caractere.")
        String city,

        Set<Amenity> amenities,

        @NotNull(message = "Alege data de la care locuința e disponibilă.")
        LocalDate availableFrom,

        @NotBlank(message = "Lipește linkul din Google Maps.")
        @Size(max = 2000, message = "Linkul poate avea cel mult 2000 de caractere.")
        String googleMapsUrl
) {
}
