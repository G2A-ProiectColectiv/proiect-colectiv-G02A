package ro.rentalplatform.listing.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.dto.ChangeListingStatusDto;
import ro.rentalplatform.listing.dto.CreateListingDto;
import ro.rentalplatform.listing.dto.ListingDto;
import ro.rentalplatform.listing.dto.ListingSummaryDto;
import ro.rentalplatform.listing.dto.UpdateListingDto;
import ro.rentalplatform.listing.service.ListingService;

@RestController
@RequestMapping("/api/listings")
@PreAuthorize("hasRole('LANDLORD')")
public class MyListingsController {

    private final ListingService listingService;

    public MyListingsController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    public ResponseEntity<ListingDto> create(@Valid @RequestBody CreateListingDto request) {
        ListingDto created = listingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListingDto> update(@PathVariable Long id,
                                             @Valid @RequestBody UpdateListingDto request) {
        return ResponseEntity.ok(listingService.update(id, request));
    }

    @GetMapping("/mine")
    public ResponseEntity<PageResponse<ListingSummaryDto>> listMine(
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        return ResponseEntity.ok(listingService.listOwned(status, pageable));
    }

    @GetMapping("/mine/{id}")
    public ResponseEntity<ListingDto> getMineById(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.getOwned(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ListingDto> changeStatus(@PathVariable Long id,
                                                   @Valid @RequestBody ChangeListingStatusDto request) {
        return ResponseEntity.ok(listingService.changeStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        listingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
