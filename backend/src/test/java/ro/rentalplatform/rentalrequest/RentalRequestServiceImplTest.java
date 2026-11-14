package ro.rentalplatform.rentalrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ro.rentalplatform.common.exception.BadRequestException;
import ro.rentalplatform.common.exception.ConflictException;
import ro.rentalplatform.common.exception.ForbiddenOperationException;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.common.security.OwnershipVerifier;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.location.domain.Coordinates;
import ro.rentalplatform.location.exception.CoordinatesNotFoundException;
import ro.rentalplatform.location.service.LocationService;
import ro.rentalplatform.rentalrequest.domain.RentalRequest;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;
import ro.rentalplatform.rentalrequest.dto.ChangeRequestStatusDto;
import ro.rentalplatform.rentalrequest.mapper.RentalRequestMapper;
import ro.rentalplatform.rentalrequest.repository.RentalRequestRepository;
import ro.rentalplatform.rentalrequest.service.RentalRequestServiceImpl;
import ro.rentalplatform.rentalrequest.service.RequestStatusPolicy;
import ro.rentalplatform.security.CurrentUserProvider;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ro.rentalplatform.rentalrequest.RentalRequestTestData.MAPS_URL;
import static ro.rentalplatform.rentalrequest.RentalRequestTestData.createDto;
import static ro.rentalplatform.rentalrequest.RentalRequestTestData.request;
import static ro.rentalplatform.rentalrequest.RentalRequestTestData.tenant;
import static ro.rentalplatform.rentalrequest.RentalRequestTestData.updateDto;

@ExtendWith(MockitoExtension.class)
class RentalRequestServiceImplTest {

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_ID = 2L;
    private static final Long REQUEST_ID = 20L;
    private static final BigDecimal FIVE_KM = new BigDecimal("5");

    @Mock
    private RentalRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LocationService locationService;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private RentalRequestMapper requestMapper;

    private RentalRequestServiceImpl service;
    private User owner;

    @BeforeEach
    void setUp() {
        service = new RentalRequestServiceImpl(requestRepository, userRepository, locationService, currentUserProvider,
                new OwnershipVerifier(), requestMapper, new RequestStatusPolicy());
        owner = tenant(OWNER_ID);
    }

    private void loggedInAs(Long userId) {
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.of(userId));
    }

    private void loggedInAsOwnerForCreate() {
        loggedInAs(OWNER_ID);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private RentalRequest existing(RequestStatus status) {
        RentalRequest request = request(REQUEST_ID, owner, status);
        when(requestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        return request;
    }

    private RentalRequest savedRequest() {
        ArgumentCaptor<RentalRequest> captor = ArgumentCaptor.forClass(RentalRequest.class);
        verify(requestRepository).save(captor.capture());
        return captor.getValue();
    }

    // ---- create ----

    @Test
    void createWithoutZoneSavesActiveRequestAndSkipsLocation() {
        loggedInAsOwnerForCreate();

        service.create(createDto(null, null));

        RentalRequest saved = savedRequest();
        assertSame(owner, saved.getTenant());
        assertEquals(RequestStatus.ACTIVE, saved.getStatus());
        assertEquals("Caut garsonieră", saved.getTitle());
        assertEquals("București", saved.getCity());
        assertEquals("Militari", saved.getArea());
        assertNull(saved.getCenterLatitude());
        assertNull(saved.getRadiusKm());
        verify(locationService, never()).parse(anyString());
    }

    @Test
    void createWithZoneTakesCenterFromTheLink() {
        // CER-03
        loggedInAsOwnerForCreate();
        when(locationService.parse(MAPS_URL)).thenReturn(new Coordinates(44.4268, 26.1025));

        service.create(createDto(MAPS_URL, FIVE_KM));

        RentalRequest saved = savedRequest();
        assertEquals(MAPS_URL, saved.getGoogleMapsUrl());
        assertEquals(44.4268, saved.getCenterLatitude());
        assertEquals(26.1025, saved.getCenterLongitude());
        assertEquals(FIVE_KM, saved.getRadiusKm());
    }

    @Test
    void linkWithoutRadiusIsRejected() {
        assertThrows(BadRequestException.class, () -> service.create(createDto(MAPS_URL, null)));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void radiusWithoutLinkIsRejected() {
        assertThrows(BadRequestException.class, () -> service.create(createDto(null, FIVE_KM)));
        assertThrows(BadRequestException.class, () -> service.create(createDto("  ", FIVE_KM)));
    }

    @Test
    void radiusOutsideOneToFiftyKmIsRejected() {
        assertThrows(BadRequestException.class, () -> service.create(createDto(MAPS_URL, new BigDecimal("0.5"))));
        assertThrows(BadRequestException.class, () -> service.create(createDto(MAPS_URL, new BigDecimal("51"))));
        verify(locationService, never()).parse(anyString());
    }

    @Test
    void radiusLimitsAreInclusive() {
        loggedInAsOwnerForCreate();
        when(locationService.parse(MAPS_URL)).thenReturn(new Coordinates(44.4268, 26.1025));

        service.create(createDto(MAPS_URL, BigDecimal.ONE));
        service.create(createDto(MAPS_URL, new BigDecimal("50")));
    }

    @Test
    void linkWithoutCoordinatesSavesNothing() {
        loggedInAs(OWNER_ID);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(locationService.parse(MAPS_URL)).thenThrow(new CoordinatesNotFoundException());

        assertThrows(CoordinatesNotFoundException.class, () -> service.create(createDto(MAPS_URL, FIVE_KM)));
        verify(requestRepository, never()).save(any());
    }

    // ---- update ----

    @Test
    void updateReplacesFieldsAndRemovesZoneWhenNoLongerSent() {
        loggedInAs(OWNER_ID);
        RentalRequest request = existing(RequestStatus.ACTIVE);
        request.setGoogleMapsUrl(MAPS_URL);
        request.setCenterLatitude(44.0);
        request.setCenterLongitude(26.0);
        request.setRadiusKm(FIVE_KM);
        when(requestRepository.save(request)).thenReturn(request);

        service.update(REQUEST_ID, updateDto(null, null));

        assertEquals("Caut apartament", request.getTitle());
        assertEquals(PropertyType.APARTMENT, request.getDesiredType());
        assertEquals("Cluj-Napoca", request.getCity());
        assertNull(request.getGoogleMapsUrl());
        assertNull(request.getCenterLatitude());
        assertNull(request.getCenterLongitude());
        assertNull(request.getRadiusKm());
    }

    @Test
    void updateWithNewZoneRecomputesCenter() {
        loggedInAs(OWNER_ID);
        RentalRequest request = existing(RequestStatus.ACTIVE);
        when(locationService.parse(MAPS_URL)).thenReturn(new Coordinates(46.7712, 23.6236));
        when(requestRepository.save(request)).thenReturn(request);

        service.update(REQUEST_ID, updateDto(MAPS_URL, new BigDecimal("10")));

        assertEquals(46.7712, request.getCenterLatitude());
        assertEquals(new BigDecimal("10"), request.getRadiusKm());
    }

    @Test
    void updateOfAnotherTenantsRequestIsForbidden() {
        // CER-05
        loggedInAs(OTHER_ID);
        RentalRequest request = existing(RequestStatus.ACTIVE);

        assertThrows(ForbiddenOperationException.class, () -> service.update(REQUEST_ID, updateDto(null, null)));
        assertEquals("Caut garsonieră", request.getTitle());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void updateOfDeletedRequestIsNotFound() {
        loggedInAs(OWNER_ID);
        existing(RequestStatus.DELETED);

        assertThrows(NotFoundException.class, () -> service.update(REQUEST_ID, updateDto(null, null)));
    }

    // ---- getOwned ----

    @Test
    void getOwnedReturnsMappedRequest() {
        loggedInAs(OWNER_ID);
        RentalRequest request = existing(RequestStatus.FOUND);
        when(requestMapper.toDto(request)).thenReturn(RentalRequestTestData.dto(REQUEST_ID, RequestStatus.FOUND));

        assertEquals(REQUEST_ID, service.getOwned(REQUEST_ID).id());
    }

    @Test
    void getOwnedOfAnotherTenantIsForbiddenAndMissingIsNotFound() {
        loggedInAs(OTHER_ID);
        existing(RequestStatus.ACTIVE);
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenOperationException.class, () -> service.getOwned(REQUEST_ID));
        assertThrows(NotFoundException.class, () -> service.getOwned(99L));
    }

    // ---- listOwned ----

    @Test
    void listOwnedWithStatusFiltersAndSortsNewestFirst() {
        // CER-08
        loggedInAs(OWNER_ID);
        when(requestRepository.findByTenantIdAndStatus(eq(OWNER_ID), eq(RequestStatus.FOUND), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(request(REQUEST_ID, owner, RequestStatus.FOUND))));

        assertEquals(1, service.listOwned(RequestStatus.FOUND, PageRequest.of(0, 10)).totalElements());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(requestRepository).findByTenantIdAndStatus(eq(OWNER_ID), eq(RequestStatus.FOUND), captor.capture());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("createdAt").getDirection());
    }

    // ---- changeStatus ----

    @Test
    void changeStatusAppliesAllowedTransition() {
        loggedInAs(OWNER_ID);
        RentalRequest request = existing(RequestStatus.ACTIVE);
        when(requestRepository.save(request)).thenReturn(request);

        service.changeStatus(REQUEST_ID, new ChangeRequestStatusDto(RequestStatus.FOUND));

        assertEquals(RequestStatus.FOUND, request.getStatus());
    }

    @Test
    void foundToClosedIsAConflict() {
        // CER-06
        loggedInAs(OWNER_ID);
        RentalRequest request = existing(RequestStatus.FOUND);

        assertThrows(ConflictException.class,
                () -> service.changeStatus(REQUEST_ID, new ChangeRequestStatusDto(RequestStatus.CLOSED)));
        assertEquals(RequestStatus.FOUND, request.getStatus());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void statusCannotBeSetToDeletedThroughChangeStatus() {
        loggedInAs(OWNER_ID);
        existing(RequestStatus.ACTIVE);

        assertThrows(ConflictException.class,
                () -> service.changeStatus(REQUEST_ID, new ChangeRequestStatusDto(RequestStatus.DELETED)));
    }

    // ---- delete ----

    @Test
    void deleteMarksRequestDeleted() {
        // CER-07
        loggedInAs(OWNER_ID);
        RentalRequest request = existing(RequestStatus.CLOSED);

        service.delete(REQUEST_ID);

        assertEquals(RequestStatus.DELETED, request.getStatus());
        verify(requestRepository).save(request);
    }

    @Test
    void deleteOfAnotherTenantsRequestIsForbidden() {
        loggedInAs(OTHER_ID);
        RentalRequest request = existing(RequestStatus.ACTIVE);

        assertThrows(ForbiddenOperationException.class, () -> service.delete(REQUEST_ID));
        assertEquals(RequestStatus.ACTIVE, request.getStatus());
    }
}
