package ro.rentalplatform.rentalrequest.controller;

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
import ro.rentalplatform.rentalrequest.domain.RequestStatus;
import ro.rentalplatform.rentalrequest.dto.ChangeRequestStatusDto;
import ro.rentalplatform.rentalrequest.dto.CreateRentalRequestDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSummaryDto;
import ro.rentalplatform.rentalrequest.dto.UpdateRentalRequestDto;
import ro.rentalplatform.rentalrequest.service.RentalRequestService;

/**
 * Endpoint-uri pentru cererile de închiriere ale chiriașului (CER-01...CER-08).
 *
 * POST   /api/requests             → 201 Created
 * PUT    /api/requests/{id}        → 200 OK
 * GET    /api/requests/mine        → 200 OK (paginat, opțional filtrat pe status)
 * GET    /api/requests/mine/{id}   → 200 OK
 * PATCH  /api/requests/{id}/status → 200 OK
 * DELETE /api/requests/{id}        → 204 No Content
 */
@RestController
@RequestMapping("/api/requests")
@PreAuthorize("hasRole('TENANT')")
public class MyRentalRequestsController {

    private final RentalRequestService requestService;

    public MyRentalRequestsController(RentalRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<RentalRequestDto> create(@Valid @RequestBody CreateRentalRequestDto dto) {
        RentalRequestDto created = requestService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RentalRequestDto> update(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateRentalRequestDto dto) {
        return ResponseEntity.ok(requestService.update(id, dto));
    }

    @GetMapping("/mine")
    public ResponseEntity<PageResponse<RentalRequestSummaryDto>> listMine(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        return ResponseEntity.ok(requestService.listOwned(status, pageable));
    }

    @GetMapping("/mine/{id}")
    public ResponseEntity<RentalRequestDto> getMineById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getOwned(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RentalRequestDto> changeStatus(@PathVariable Long id,
                                                         @Valid @RequestBody ChangeRequestStatusDto dto) {
        return ResponseEntity.ok(requestService.changeStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        requestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
