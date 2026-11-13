package ro.rentalplatform.rentalrequest.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.rentalrequest.validation.BudgetRangeHolder;
import ro.rentalplatform.rentalrequest.validation.ValidBudgetRange;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO folosit la actualizarea unei cereri de închiriere.
 * Aceleași validări ca la creare.
 */
@ValidBudgetRange
public record UpdateRentalRequestDto(

        @NotBlank(message = "Introdu titlul cererii.")
        @Size(max = 150, message = "Titlul poate avea cel mult 150 de caractere.")
        String title,

        @NotBlank(message = "Scrie o descriere.")
        String description,

        @NotNull(message = "Alege tipul locuinței dorite.")
        PropertyType desiredType,

        @NotNull(message = "Introdu bugetul minim.")
        @PositiveOrZero(message = "Bugetul minim trebuie să fie ≥ 0.")
        BigDecimal budgetMin,

        @NotNull(message = "Introdu bugetul maxim.")
        @PositiveOrZero(message = "Bugetul maxim trebuie să fie ≥ 0.")
        BigDecimal budgetMax,

        @NotNull(message = "Alege moneda.")
        Currency currency,

        @NotBlank(message = "Introdu orașul.")
        @Size(max = 100, message = "Orașul poate avea cel mult 100 de caractere.")
        String city,

        @Size(max = 100, message = "Zona poate avea cel mult 100 de caractere.")
        String area,

        Short rooms,

        @NotNull(message = "Alege data de la care cauți o locuință.")
        @FutureOrPresent(message = "Data nu poate fi în trecut.")
        LocalDate desiredFrom,

        @Size(max = 2000, message = "Linkul poate avea cel mult 2000 de caractere.")
        String googleMapsUrl,

        BigDecimal radiusKm

) implements BudgetRangeHolder {

    @Override
    public BigDecimal getBudgetMin() {
        return budgetMin;
    }

    @Override
    public BigDecimal getBudgetMax() {
        return budgetMax;
    }
}
