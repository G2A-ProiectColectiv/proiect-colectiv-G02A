package ro.rentalplatform.rentalrequest.service;

import org.springframework.data.domain.Pageable;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;
import ro.rentalplatform.rentalrequest.dto.ChangeRequestStatusDto;
import ro.rentalplatform.rentalrequest.dto.CreateRentalRequestDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSummaryDto;
import ro.rentalplatform.rentalrequest.dto.UpdateRentalRequestDto;

public interface RentalRequestService {

    /** Crează o cerere nouă; returnează 201. */
    RentalRequestDto create(CreateRentalRequestDto dto);

    /** Actualizează o cerere existentă (CER-05: 403 / 404 dacă nu e a utilizatorului). */
    RentalRequestDto update(Long id, UpdateRentalRequestDto dto);

    /** Returnează cererea completă (CER-05). */
    RentalRequestDto getOwned(Long id);

    /** Listare paginată, opțional filtrată pe status (CER-08). */
    PageResponse<RentalRequestSummaryDto> listOwned(RequestStatus status, Pageable pageable);

    /** Schimbă statusul conform tranziților permise (CER-06). */
    RentalRequestDto changeStatus(Long id, ChangeRequestStatusDto dto);

    /** Ștergere logică – statusul devine DELETED (CER-07). */
    void delete(Long id);
}
