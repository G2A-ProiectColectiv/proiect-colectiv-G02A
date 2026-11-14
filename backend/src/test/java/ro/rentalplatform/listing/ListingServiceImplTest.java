package ro.rentalplatform.listing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ro.rentalplatform.common.exception.ConflictException;
import ro.rentalplatform.common.exception.ForbiddenOperationException;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.common.security.OwnershipVerifier;
import ro.rentalplatform.listing.domain.Amenity;
import ro.rentalplatform.listing.domain.Listing;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.listing.event.ListingsDeletedEvent;
import ro.rentalplatform.listing.mapper.ListingMapper;
import ro.rentalplatform.listing.repository.ListingRepository;
import ro.rentalplatform.listing.service.ListingServiceImpl;
import ro.rentalplatform.listing.service.ListingStatusPolicy;
import ro.rentalplatform.location.domain.Coordinates;
import ro.rentalplatform.location.exception.CoordinatesNotFoundException;
import ro.rentalplatform.location.service.LocationService;
import ro.rentalplatform.security.CurrentUserProvider;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ro.rentalplatform.listing.ListingTestData.MAPS_URL;
import static ro.rentalplatform.listing.ListingTestData.createDto;
import static ro.rentalplatform.listing.ListingTestData.landlord;
import static ro.rentalplatform.listing.ListingTestData.listing;
import static ro.rentalplatform.listing.ListingTestData.updateDto;

@ExtendWith(MockitoExtension.class)
class ListingServiceImplTest {

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_ID = 2L;
    private static final Long LISTING_ID = 10L;

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LocationService locationService;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private ListingMapper listingMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ListingServiceImpl service;
    private User owner;

    @BeforeEach
    void setUp() {
        // real ownership verifier and status policy: they are simple and part of the behaviour under test
        service = new ListingServiceImpl(listingRepository, userRepository, locationService, currentUserProvider,
                new OwnershipVerifier(), listingMapper, new ListingStatusPolicy(), eventPublisher);
        owner = landlord(OWNER_ID);
    }

    private void loggedInAs(Long userId) {
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.of(userId));
    }

    private Listing existing(ListingStatus status) {
        Listing listing = listing(LISTING_ID, owner, status);
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        return listing;
    }

    // ---- create ----

    @Test
    void createSavesActiveListingWithServerExtractedCoordinates() {
        loggedInAs(OWNER_ID);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(locationService.parse(" " + MAPS_URL + " ")).thenReturn(new Coordinates(44.4268, 26.1025));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(createDto(MAPS_URL, Set.of(Amenity.BALCONY, Amenity.PARKING)));

        ArgumentCaptor<Listing> captor = ArgumentCaptor.forClass(Listing.class);
        verify(listingRepository).save(captor.capture());
        Listing saved = captor.getValue();
        assertSame(owner, saved.getLandlord());
        assertEquals(ListingStatus.ACTIVE, saved.getStatus());
        // LOC-04: coordinates come from the link, not from the client
        assertEquals(44.4268, saved.getLatitude());
        assertEquals(26.1025, saved.getLongitude());
        assertEquals(MAPS_URL, saved.getGoogleMapsUrl());
        assertEquals("Apartament 2 camere", saved.getTitle());
        assertEquals("București", saved.getCity());
        assertEquals(Set.of(Amenity.BALCONY, Amenity.PARKING), saved.getAmenities());
    }

    @Test
    void createWithoutAmenitiesStoresEmptySet() {
        loggedInAs(OWNER_ID);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(locationService.parse(anyString())).thenReturn(new Coordinates(44.4268, 26.1025));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(createDto(MAPS_URL, null));

        ArgumentCaptor<Listing> captor = ArgumentCaptor.forClass(Listing.class);
        verify(listingRepository).save(captor.capture());
        assertTrue(captor.getValue().getAmenities().isEmpty());
    }

    @Test
    void createWithLinkWithoutCoordinatesSavesNothing() {
        loggedInAs(OWNER_ID);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(locationService.parse(anyString())).thenThrow(new CoordinatesNotFoundException());

        assertThrows(CoordinatesNotFoundException.class, () -> service.create(createDto(MAPS_URL, null)));
        verify(listingRepository, never()).save(any());
    }

    // ---- update ----

    @Test
    void updateChangesFieldsCoordinatesAndReplacesAmenities() {
        loggedInAs(OWNER_ID);
        Listing listing = existing(ListingStatus.ACTIVE);
        listing.getAmenities().add(Amenity.PARKING);
        when(locationService.parse(MAPS_URL)).thenReturn(new Coordinates(46.7712, 23.6236));
        when(listingRepository.save(listing)).thenReturn(listing);

        service.update(LISTING_ID, updateDto("Garsonieră", Set.of(Amenity.INTERNET)));

        assertEquals("Garsonieră", listing.getTitle());
        assertEquals(PropertyType.STUDIO, listing.getType());
        assertEquals("Cluj-Napoca", listing.getCity());
        assertEquals(46.7712, listing.getLatitude());
        assertEquals(23.6236, listing.getLongitude());
        assertEquals(Set.of(Amenity.INTERNET), listing.getAmenities());
    }

    @Test
    void updateOfAnotherLandlordsListingIsForbidden() {
        // GEST-02
        loggedInAs(OTHER_ID);
        Listing listing = existing(ListingStatus.ACTIVE);

        assertThrows(ForbiddenOperationException.class,
                () -> service.update(LISTING_ID, updateDto("Furat", null)));
        assertEquals("Apartament 2 camere", listing.getTitle());
        verify(locationService, never()).parse(anyString());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void updateOfDeletedListingIsNotFound() {
        loggedInAs(OWNER_ID);
        existing(ListingStatus.DELETED);

        assertThrows(NotFoundException.class, () -> service.update(LISTING_ID, updateDto("X", null)));
    }

    // ---- getOwned ----

    @Test
    void getOwnedReturnsMappedListing() {
        loggedInAs(OWNER_ID);
        Listing listing = existing(ListingStatus.RENTED);
        when(listingMapper.toDto(listing)).thenReturn(ListingTestData.dto(LISTING_ID, ListingStatus.RENTED));

        assertEquals(LISTING_ID, service.getOwned(LISTING_ID).id());
    }

    @Test
    void getOwnedOfAnotherLandlordIsForbidden() {
        loggedInAs(OTHER_ID);
        existing(ListingStatus.ACTIVE);

        assertThrows(ForbiddenOperationException.class, () -> service.getOwned(LISTING_ID));
    }

    @Test
    void getOwnedOfMissingListingIsNotFound() {
        loggedInAs(OWNER_ID);
        when(listingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getOwned(99L));
    }

    // ---- listOwned ----

    @Test
    void listOwnedWithoutStatusExcludesDeletedAndSortsNewestFirst() {
        loggedInAs(OWNER_ID);
        when(listingRepository.findByLandlordIdAndStatusNot(eq(OWNER_ID), eq(ListingStatus.DELETED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(listing(LISTING_ID, owner, ListingStatus.ACTIVE))));

        service.listOwned(null, PageRequest.of(0, 10));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(listingRepository).findByLandlordIdAndStatusNot(eq(OWNER_ID), eq(ListingStatus.DELETED), captor.capture());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("createdAt").getDirection());
    }

    @Test
    void listOwnedWithStatusFiltersByThatStatus() {
        loggedInAs(OWNER_ID);
        when(listingRepository.findByLandlordIdAndStatus(eq(OWNER_ID), eq(ListingStatus.RENTED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertEquals(0, service.listOwned(ListingStatus.RENTED, PageRequest.of(0, 10)).totalElements());
    }

    @Test
    void listOwnedNeverShowsDeletedEvenWhenAskedFor() {
        loggedInAs(OWNER_ID);
        when(listingRepository.findByLandlordIdAndStatusNot(eq(OWNER_ID), eq(ListingStatus.DELETED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.listOwned(ListingStatus.DELETED, PageRequest.of(0, 10));

        verify(listingRepository, never()).findByLandlordIdAndStatus(any(), any(), any());
    }

    // ---- changeStatus ----

    @Test
    void changeStatusAppliesAllowedTransition() {
        loggedInAs(OWNER_ID);
        Listing listing = existing(ListingStatus.ACTIVE);
        when(listingRepository.save(listing)).thenReturn(listing);

        service.changeStatus(LISTING_ID, ListingStatus.RENTED);

        assertEquals(ListingStatus.RENTED, listing.getStatus());
    }

    @Test
    void changeStatusRejectsForbiddenTransitionWithConflict() {
        // GEST-03: RENTED -> UNAVAILABLE must go through ACTIVE
        loggedInAs(OWNER_ID);
        Listing listing = existing(ListingStatus.RENTED);

        assertThrows(ConflictException.class, () -> service.changeStatus(LISTING_ID, ListingStatus.UNAVAILABLE));
        assertEquals(ListingStatus.RENTED, listing.getStatus());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void changeStatusOfAnotherLandlordsListingIsForbidden() {
        loggedInAs(OTHER_ID);
        existing(ListingStatus.ACTIVE);

        assertThrows(ForbiddenOperationException.class, () -> service.changeStatus(LISTING_ID, ListingStatus.RENTED));
    }

    // ---- delete ----

    @Test
    void deleteMarksListingDeletedClearsImagesAndPublishesEvent() {
        loggedInAs(OWNER_ID);
        Listing listing = existing(ListingStatus.ACTIVE);
        listing.getImages().add(ListingTestData.image(100L, listing, 0, true));

        service.delete(LISTING_ID);

        assertEquals(ListingStatus.DELETED, listing.getStatus());
        assertTrue(listing.getImages().isEmpty());
        verify(listingRepository).save(listing);
        // POZE-05: the files are removed from disk by the listener, after commit
        verify(eventPublisher).publishEvent(new ListingsDeletedEvent(List.of(LISTING_ID)));
    }

    @Test
    void deleteOfAnotherLandlordsListingIsForbiddenAndPublishesNothing() {
        loggedInAs(OTHER_ID);
        Listing listing = existing(ListingStatus.ACTIVE);

        assertThrows(ForbiddenOperationException.class, () -> service.delete(LISTING_ID));
        assertEquals(ListingStatus.ACTIVE, listing.getStatus());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }
}
