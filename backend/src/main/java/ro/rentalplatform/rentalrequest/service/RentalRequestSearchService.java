package ro.rentalplatform.rentalrequest.service;

import org.springframework.data.domain.Pageable;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.rentalrequest.dto.RentalRequestDetailsDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSearchCriteria;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSummaryDto;

/**
 * Citirile cererilor pentru proprietari (căutare și detalii).
 * Modificările rămân în {@link RentalRequestService}.
 */
public interface RentalRequestSearchService {

    /** Căutare paginată, doar cereri ACTIVE ale chiriașilor activi (CAUT-02, CAUT-05). */
    PageResponse<RentalRequestSummaryDto> search(RentalRequestSearchCriteria criteria, Pageable pageable);

    /**
     * Detaliile unei cereri (DET-05). Telefonul chiriașului se trimite doar dacă cererea vine de la
     * un utilizator autentificat. Cerere inexistentă sau care nu e ACTIVE → 404.
     */
    RentalRequestDetailsDto getDetails(Long id, boolean viewerAuthenticated);
}
