package ro.rentalplatform.listing.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ro.rentalplatform.listing.domain.Listing;
import ro.rentalplatform.listing.domain.ListingStatus;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    Page<Listing> findByLandlordIdAndStatusNot(Long landlordId, ListingStatus excludedStatus, Pageable pageable);

    Page<Listing> findByLandlordIdAndStatus(Long landlordId, ListingStatus status, Pageable pageable);

    List<Listing> findByLandlordIdAndStatusNot(Long landlordId, ListingStatus excludedStatus);
}
