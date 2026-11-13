package ro.rentalplatform.listing.service;

import org.springframework.web.multipart.MultipartFile;
import ro.rentalplatform.listing.dto.ListingImageDto;

import java.util.List;

public interface ListingImageService {

    List<ListingImageDto> addImages(Long listingId, List<MultipartFile> files);

    void removeImage(Long listingId, Long imageId);

    List<ListingImageDto> setPrimary(Long listingId, Long imageId);

    List<ListingImageDto> reorder(Long listingId, List<Long> imageIds);
}
