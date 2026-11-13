package ro.rentalplatform.listing.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ro.rentalplatform.listing.dto.ListingImageDto;
import ro.rentalplatform.listing.dto.ReorderImagesDto;
import ro.rentalplatform.listing.service.ListingImageService;

import java.util.List;

@RestController
@RequestMapping("/api/listings/{listingId}/images")
@PreAuthorize("hasRole('LANDLORD')")
public class ListingImageController {

    private final ListingImageService listingImageService;

    public ListingImageController(ListingImageService listingImageService) {
        this.listingImageService = listingImageService;
    }

    @PostMapping
    public ResponseEntity<List<ListingImageDto>> upload(@PathVariable Long listingId,
                                                        @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(listingImageService.addImages(listingId, files));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> remove(@PathVariable Long listingId,
                                       @PathVariable Long imageId) {
        listingImageService.removeImage(listingId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{imageId}/primary")
    public ResponseEntity<List<ListingImageDto>> setPrimary(@PathVariable Long listingId,
                                                            @PathVariable Long imageId) {
        return ResponseEntity.ok(listingImageService.setPrimary(listingId, imageId));
    }

    @PutMapping("/order")
    public ResponseEntity<List<ListingImageDto>> reorder(@PathVariable Long listingId,
                                                         @Valid @RequestBody ReorderImagesDto request) {
        return ResponseEntity.ok(listingImageService.reorder(listingId, request.imageIds()));
    }
}
