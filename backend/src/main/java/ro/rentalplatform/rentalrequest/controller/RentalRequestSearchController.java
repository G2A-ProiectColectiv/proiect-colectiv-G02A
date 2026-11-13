package ro.rentalplatform.rentalrequest.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.common.web.SortParser;
import ro.rentalplatform.rentalrequest.dto.RentalRequestDetailsDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSearchCriteria;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSummaryDto;
import ro.rentalplatform.rentalrequest.service.RentalRequestSearchService;

import java.util.Set;

/**
 * Căutarea cererilor de către proprietari (CAUT-02, DET-05):
 * <pre>
 * GET /api/requests?…filtre&amp;sort=budgetMax,desc&amp;page=0&amp;size=12  → PageResponse&lt;RentalRequestSummaryDto&gt;
 * GET /api/requests/{id}                                         → RentalRequestDetailsDto
 * </pre>
 * Doar LANDLORD. {@code /api/requests/mine/**} rămâne în {@link MyRentalRequestsController}, doar pentru TENANT.
 */
@RestController
@RequestMapping("/api/requests")
@PreAuthorize("hasRole('LANDLORD')")
public class RentalRequestSearchController {

    private static final Set<String> SORT_FIELDS = Set.of("createdAt", "budgetMin", "budgetMax", "desiredFrom");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");
    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final int MAX_PAGE_SIZE = 100;

    private final RentalRequestSearchService searchService;

    public RentalRequestSearchController(RentalRequestSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<RentalRequestSummaryDto>> search(
            @Valid @ModelAttribute RentalRequestSearchCriteria criteria,
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
        return ResponseEntity.ok(searchService.search(criteria, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalRequestDetailsDto> details(@PathVariable Long id) {
        return ResponseEntity.ok(searchService.getDetails(id));
    }
}
