package ro.rentalplatform.rentalrequest.dto;

import jakarta.validation.constraints.NotNull;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;

/**
 * DTO pentru schimbarea statusului unei cereri.
 * Statusuri permise prin API: FOUND, CLOSED, ACTIVE.
 * Tranziția DELETED se face exclusiv prin DELETE.
 */
public record ChangeRequestStatusDto(

        @NotNull(message = "Statusul este obligatoriu.")
        RequestStatus status

) {
}
