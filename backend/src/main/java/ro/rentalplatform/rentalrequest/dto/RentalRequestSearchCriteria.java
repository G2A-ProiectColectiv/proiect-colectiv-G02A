package ro.rentalplatform.rentalrequest.dto;

import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;
import ro.rentalplatform.listing.domain.PropertyType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Filtrele cu care un proprietar caută cereri (CAUT-02). Orice filtru lipsă este ignorat.
 *
 * @param minBudget   împreună cu {@code maxBudget}, formează intervalul de preț căutat; o cerere apare
 *                    dacă bugetul ei se suprapune cu acest interval
 * @param area        caută textul în zona cererii, fără să conteze literele mari și mici
 * @param minRooms    cererea trebuie să ceară cel puțin atâtea camere
 * @param desiredFrom cererea trebuie să înceapă cel mai devreme la această dată ({@code desiredFrom >= dată}),
 *                    adică după ce locuința proprietarului devine disponibilă
 */
public record RentalRequestSearchCriteria(
        PropertyType type,

        @PositiveOrZero(message = "Bugetul minim trebuie să fie 0 sau mai mare.")
        BigDecimal minBudget,

        @PositiveOrZero(message = "Bugetul maxim trebuie să fie 0 sau mai mare.")
        BigDecimal maxBudget,

        String city,

        String area,

        @PositiveOrZero(message = "Numărul minim de camere trebuie să fie 0 sau mai mare.")
        Short minRooms,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate desiredFrom
) {
}
