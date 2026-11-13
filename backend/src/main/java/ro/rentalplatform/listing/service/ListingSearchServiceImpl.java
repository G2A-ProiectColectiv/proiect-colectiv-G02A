package ro.rentalplatform.listing.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.listing.domain.Listing;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.dto.ListingDetailsDto;
import ro.rentalplatform.listing.dto.ListingMapMarkerDto;
import ro.rentalplatform.listing.dto.ListingSearchCriteria;
import ro.rentalplatform.listing.dto.ListingSummaryDto;
import ro.rentalplatform.listing.mapper.ListingMapper;
import ro.rentalplatform.listing.repository.ListingRepository;
import ro.rentalplatform.listing.repository.ListingSpecifications;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;

import java.util.List;

@Service
public class ListingSearchServiceImpl implements ListingSearchService {

    private static final String NOT_AVAILABLE = "Anunțul nu mai este disponibil.";

    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;

    public ListingSearchServiceImpl(ListingRepository listingRepository, ListingMapper listingMapper) {
        this.listingRepository = listingRepository;
        this.listingMapper = listingMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ListingSummaryDto> search(ListingSearchCriteria criteria, Pageable pageable) {
        Specification<Listing> spec = ListingSpecifications.fromCriteria(criteria);
        return PageResponse.from(listingRepository.findAll(spec, pageable).map(listingMapper::toSummaryDto));
    }

    @Override
    @Transactional(readOnly = true)
    public ListingDetailsDto getPublicDetails(Long id, boolean viewerAuthenticated) {
        Listing listing = listingRepository.findById(id)
                .filter(candidate -> candidate.getStatus() == ListingStatus.ACTIVE)
                .filter(candidate -> candidate.getLandlord() != null
                        && candidate.getLandlord().getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(NOT_AVAILABLE));

        return toDetailsDto(listing, viewerAuthenticated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingMapMarkerDto> findForMap(ListingSearchCriteria criteria, BoundingBox bbox) {
        Specification<Listing> spec = ListingSpecifications.fromCriteria(criteria)
                .and(ListingSpecifications.inBoundingBox(bbox.south(), bbox.west(), bbox.north(), bbox.east()));

        Pageable limit = PageRequest.of(0, MAX_MAP_MARKERS, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return listingRepository.findAll(spec, limit).getContent().stream()
                .map(listingMapper::toMarkerDto)
                .toList();
    }

    private ListingDetailsDto toDetailsDto(Listing listing, boolean viewerAuthenticated) {
        User landlord = listing.getLandlord();
        ListingDetailsDto.Owner owner = new ListingDetailsDto.Owner(
                landlord.getId(),
                landlord.getFirstName(),
                landlord.getLastName(),
                landlord.getAvatarUrl(),
                viewerAuthenticated ? landlord.getPhone() : null
        );

        return new ListingDetailsDto(
                listing.getId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getType(),
                listing.getPrice(),
                listing.getCurrency(),
                listing.getRooms(),
                listing.getBathrooms(),
                listing.getAreaSqm(),
                listing.getAddress(),
                listing.getCity(),
                listingMapper.mapAmenities(listing.getAmenities()),
                listing.getAvailableFrom(),
                listing.getGoogleMapsUrl(),
                listing.getLatitude(),
                listing.getLongitude(),
                listing.getStatus(),
                listingMapper.mapImages(listing.getImages()),
                owner,
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }
}
