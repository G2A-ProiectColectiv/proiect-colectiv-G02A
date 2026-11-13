package ro.rentalplatform.listing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.rentalplatform.listing.domain.ListingImage;

import java.util.List;
import java.util.Optional;

public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {

    List<ListingImage> findByListingIdOrderByPositionAscIdAsc(Long listingId);

    Optional<ListingImage> findByIdAndListingId(Long id, Long listingId);

    long countByListingId(Long listingId);
}
