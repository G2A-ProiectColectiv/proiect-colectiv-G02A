package ro.rentalplatform.listing.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.common.exception.ConflictException;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.common.security.OwnershipVerifier;
import ro.rentalplatform.listing.domain.Listing;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.dto.CreateListingDto;
import ro.rentalplatform.listing.dto.ListingDto;
import ro.rentalplatform.listing.dto.ListingSummaryDto;
import ro.rentalplatform.listing.dto.UpdateListingDto;
import ro.rentalplatform.listing.event.ListingsDeletedEvent;
import ro.rentalplatform.listing.mapper.ListingMapper;
import ro.rentalplatform.listing.repository.ListingRepository;
import ro.rentalplatform.location.domain.Coordinates;
import ro.rentalplatform.location.service.LocationService;
import ro.rentalplatform.security.CurrentUserProvider;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.repository.UserRepository;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final CurrentUserProvider currentUserProvider;
    private final OwnershipVerifier ownershipVerifier;
    private final ListingMapper listingMapper;
    private final ListingStatusPolicy listingStatusPolicy;
    private final ApplicationEventPublisher eventPublisher;

    public ListingServiceImpl(ListingRepository listingRepository,
                              UserRepository userRepository,
                              LocationService locationService,
                              CurrentUserProvider currentUserProvider,
                              OwnershipVerifier ownershipVerifier,
                              ListingMapper listingMapper,
                              ListingStatusPolicy listingStatusPolicy,
                              ApplicationEventPublisher eventPublisher) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.locationService = locationService;
        this.currentUserProvider = currentUserProvider;
        this.ownershipVerifier = ownershipVerifier;
        this.listingMapper = listingMapper;
        this.listingStatusPolicy = listingStatusPolicy;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ListingDto create(CreateListingDto dto) {
        User landlord = getCurrentUser();
        Coordinates coordinates = locationService.parse(dto.googleMapsUrl());

        Listing listing = Listing.builder()
                .landlord(landlord)
                .title(dto.title().trim())
                .description(dto.description().trim())
                .type(dto.type())
                .price(dto.price())
                .currency(dto.currency())
                .rooms(dto.rooms())
                .bathrooms(dto.bathrooms())
                .areaSqm(dto.areaSqm())
                .address(dto.address().trim())
                .city(dto.city().trim())
                .latitude(coordinates.latitude())
                .longitude(coordinates.longitude())
                .googleMapsUrl(dto.googleMapsUrl().trim())
                .availableFrom(dto.availableFrom())
                .status(ListingStatus.ACTIVE)
                .amenities(dto.amenities() != null ? new LinkedHashSet<>(dto.amenities()) : new LinkedHashSet<>())
                .build();

        Listing saved = listingRepository.save(listing);
        return listingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ListingDto update(Long id, UpdateListingDto dto) {
        Listing listing = findOwnedActiveOrNonDeleted(id);
        Coordinates coordinates = locationService.parse(dto.googleMapsUrl());

        listing.setTitle(dto.title().trim());
        listing.setDescription(dto.description().trim());
        listing.setType(dto.type());
        listing.setPrice(dto.price());
        listing.setCurrency(dto.currency());
        listing.setRooms(dto.rooms());
        listing.setBathrooms(dto.bathrooms());
        listing.setAreaSqm(dto.areaSqm());
        listing.setAddress(dto.address().trim());
        listing.setCity(dto.city().trim());
        listing.setLatitude(coordinates.latitude());
        listing.setLongitude(coordinates.longitude());
        listing.setGoogleMapsUrl(dto.googleMapsUrl().trim());
        listing.setAvailableFrom(dto.availableFrom());
        listing.getAmenities().clear();
        if (dto.amenities() != null) {
            listing.getAmenities().addAll(dto.amenities());
        }

        Listing saved = listingRepository.save(listing);
        return listingMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ListingDto getOwned(Long id) {
        Listing listing = findOwnedActiveOrNonDeleted(id);
        return listingMapper.toDto(listing);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ListingSummaryDto> listOwned(ListingStatus status, Pageable pageable) {
        Long landlordId = getCurrentUserId();
        Pageable sortedPageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt", "id")
                );

        Page<Listing> page;
        if (status == null || status == ListingStatus.DELETED) {
            page = listingRepository.findByLandlordIdAndStatusNot(landlordId, ListingStatus.DELETED, sortedPageable);
        } else {
            page = listingRepository.findByLandlordIdAndStatus(landlordId, status, sortedPageable);
        }
        return PageResponse.from(page.map(listingMapper::toSummaryDto));
    }

    @Override
    @Transactional
    public ListingDto changeStatus(Long id, ListingStatus newStatus) {
        Listing listing = findOwnedActiveOrNonDeleted(id);
        if (!listingStatusPolicy.isAllowed(listing.getStatus(), newStatus)) {
            throw new ConflictException(
                    "Schimbarea statusului din " + listing.getStatus() + " în " + newStatus + " nu este permisă."
            );
        }
        listing.setStatus(newStatus);
        Listing saved = listingRepository.save(listing);
        return listingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Listing listing = findOwnedActiveOrNonDeleted(id);
        listing.setStatus(ListingStatus.DELETED);
        listing.getImages().clear();
        listingRepository.save(listing);
        eventPublisher.publishEvent(new ListingsDeletedEvent(List.of(listing.getId())));
    }

    private Listing findOwnedActiveOrNonDeleted(Long id) {
        Long currentUserId = getCurrentUserId();
        Listing listing = listingRepository.findById(id)
                .filter(candidate -> candidate.getStatus() != ListingStatus.DELETED)
                .orElseThrow(() -> new NotFoundException("Anunțul nu a fost găsit."));
        ownershipVerifier.verify(listing, currentUserId);
        return listing;
    }

    private Long getCurrentUserId() {
        return currentUserProvider.getCurrentUserId()
                .orElseThrow(() -> new NotFoundException("Utilizatorul nu a fost găsit."));
    }

    private User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilizatorul nu a fost găsit."));
    }
}
