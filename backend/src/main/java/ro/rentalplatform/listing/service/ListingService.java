package ro.rentalplatform.listing.service;

import org.springframework.data.domain.Pageable;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.dto.CreateListingDto;
import ro.rentalplatform.listing.dto.ListingDto;
import ro.rentalplatform.listing.dto.ListingSummaryDto;
import ro.rentalplatform.listing.dto.UpdateListingDto;

public interface ListingService {

    ListingDto create(CreateListingDto dto);

    ListingDto update(Long id, UpdateListingDto dto);

    ListingDto getOwned(Long id);

    PageResponse<ListingSummaryDto> listOwned(ListingStatus status, Pageable pageable);

    ListingDto changeStatus(Long id, ListingStatus newStatus);

    void delete(Long id);
}
