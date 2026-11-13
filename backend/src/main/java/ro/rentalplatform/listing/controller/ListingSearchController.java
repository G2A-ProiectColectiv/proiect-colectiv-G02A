package ro.rentalplatform.listing.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.common.web.SortParser;
import ro.rentalplatform.listing.dto.ListingDetailsDto;
import ro.rentalplatform.listing.dto.ListingMapMarkerDto;
import ro.rentalplatform.listing.dto.ListingSearchCriteria;
import ro.rentalplatform.listing.dto.ListingSummaryDto;
import ro.rentalplatform.listing.service.ListingSearchService;
import ro.rentalplatform.security.CurrentUserProvider;

import java.util.List;
import java.util.Set;

/**
 * Endpoint-urile publice pentru anunțuri (accesibile și vizitatorilor):
 * <pre>
 * GET /api/listings?…filtre&amp;sort=price,asc&amp;page=0&amp;size=12   → PageResponse&lt;ListingSummaryDto&gt;
 * GET /api/listings/map?bbox=south,west,north,east&amp;…filtre    → List&lt;ListingMapMarkerDto&gt; (max. 500)
 * GET /api/listings/{id}                                       → ListingDetailsDto
 * </pre>
 * {@code /api/listings/mine/**} rămâne în {@link MyListingsController}, doar pentru LANDLORD.
 */
@RestController
@RequestMapping("/api/listings")
public class ListingSearchController {

    private static final Set<String> SORT_FIELDS = Set.of("price", "createdAt");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");
    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final int MAX_PAGE_SIZE = 100;

    private final ListingSearchService listingSearchService;
    private final CurrentUserProvider currentUserProvider;

    public ListingSearchController(ListingSearchService listingSearchService,
                                   CurrentUserProvider currentUserProvider) {
        this.listingSearchService = listingSearchService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ListingSummaryDto>> search(
            @Valid @ModelAttribute ListingSearchCriteria criteria,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {
        Sort parsedSort = SortParser.parse(sort, SORT_FIELDS, DEFAULT_SORT);
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), MAX_PAGE_SIZE),
                parsedSort
        );
        return ResponseEntity.ok(listingSearchService.search(criteria, pageable));
    }

    @GetMapping("/map")
    public ResponseEntity<List<ListingMapMarkerDto>> map(
            @RequestParam(required = false) String bbox,
            @Valid @ModelAttribute ListingSearchCriteria criteria
    ) {
        ListingSearchService.BoundingBox box = ListingSearchService.BoundingBox.parse(bbox);
        return ResponseEntity.ok(listingSearchService.findForMap(criteria, box));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingDetailsDto> details(@PathVariable Long id) {
        boolean authenticated = currentUserProvider.getCurrentUserId().isPresent();
        return ResponseEntity.ok(listingSearchService.getPublicDetails(id, authenticated));
    }
}
