package ro.rentalplatform.listing.service;

import org.springframework.data.domain.Pageable;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.common.exception.BadRequestException;
import ro.rentalplatform.common.exception.FieldErrorDto;
import ro.rentalplatform.listing.dto.ListingDetailsDto;
import ro.rentalplatform.listing.dto.ListingMapMarkerDto;
import ro.rentalplatform.listing.dto.ListingSearchCriteria;
import ro.rentalplatform.listing.dto.ListingSummaryDto;

import java.util.List;

/**
 * Citirile publice ale anunțurilor (căutare, detalii, hartă).
 * Modificările rămân în {@link ListingService}.
 */
public interface ListingSearchService {

    /** Numărul maxim de markere întoarse pentru hartă (HARTA-04). */
    int MAX_MAP_MARKERS = 500;

    /** Căutare paginată, doar anunțuri ACTIVE ale proprietarilor activi (CAUT-01, CAUT-05). */
    PageResponse<ListingSummaryDto> search(ListingSearchCriteria criteria, Pageable pageable);

    /**
     * Detaliile publice ale unui anunț (DET-01).
     * Anunț inexistent sau care nu e ACTIVE → 404 (DET-04).
     *
     * @param viewerAuthenticated dacă e {@code false}, telefonul proprietarului nu se trimite (DET-03)
     */
    ListingDetailsDto getPublicDetails(Long id, boolean viewerAuthenticated);

    /** Anunțurile din zona vizibilă a hărții, cu aceleași filtre ca lista (HARTA-03, HARTA-04). */
    List<ListingMapMarkerDto> findForMap(ListingSearchCriteria criteria, BoundingBox bbox);

    /** Dreptunghiul vizibil pe hartă. */
    record BoundingBox(double south, double west, double north, double east) {

        private static final String PARAM = "bbox";

        /** Citește {@code "south,west,north,east"}; orice valoare invalidă dă 400. */
        public static BoundingBox parse(String value) {
            if (value == null || value.isBlank()) {
                throw invalid("Parametrul bbox este obligatoriu (south,west,north,east).");
            }
            String[] parts = value.split(",");
            if (parts.length != 4) {
                throw invalid("Parametrul bbox trebuie să aibă forma south,west,north,east.");
            }
            double[] numbers = new double[4];
            for (int i = 0; i < 4; i++) {
                try {
                    numbers[i] = Double.parseDouble(parts[i].trim());
                } catch (NumberFormatException ex) {
                    throw invalid("Parametrul bbox conține o valoare care nu este număr.");
                }
                if (!Double.isFinite(numbers[i])) {
                    throw invalid("Parametrul bbox conține o valoare invalidă.");
                }
            }
            double south = numbers[0];
            double west = numbers[1];
            double north = numbers[2];
            double east = numbers[3];

            if (south < -90 || south > 90 || north < -90 || north > 90) {
                throw invalid("Latitudinile din bbox trebuie să fie între -90 și 90.");
            }
            if (west < -180 || west > 180 || east < -180 || east > 180) {
                throw invalid("Longitudinile din bbox trebuie să fie între -180 și 180.");
            }
            if (south > north) {
                throw invalid("În bbox, latitudinea de sud nu poate fi mai mare decât cea de nord.");
            }
            return new BoundingBox(south, west, north, east);
        }

        private static BadRequestException invalid(String message) {
            return new BadRequestException(message, List.of(new FieldErrorDto(PARAM, message)));
        }
    }
}
