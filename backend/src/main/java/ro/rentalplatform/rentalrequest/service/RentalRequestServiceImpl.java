package ro.rentalplatform.rentalrequest.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.common.exception.BadRequestException;
import ro.rentalplatform.common.exception.ConflictException;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.common.security.OwnershipVerifier;
import ro.rentalplatform.location.domain.Coordinates;
import ro.rentalplatform.location.service.LocationService;
import ro.rentalplatform.rentalrequest.domain.RentalRequest;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;
import ro.rentalplatform.rentalrequest.dto.ChangeRequestStatusDto;
import ro.rentalplatform.rentalrequest.dto.CreateRentalRequestDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSummaryDto;
import ro.rentalplatform.rentalrequest.dto.UpdateRentalRequestDto;
import ro.rentalplatform.rentalrequest.mapper.RentalRequestMapper;
import ro.rentalplatform.rentalrequest.repository.RentalRequestRepository;
import ro.rentalplatform.security.CurrentUserProvider;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.repository.UserRepository;

import java.math.BigDecimal;

@Service
public class RentalRequestServiceImpl implements RentalRequestService {

    private final RentalRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final CurrentUserProvider currentUserProvider;
    private final OwnershipVerifier ownershipVerifier;
    private final RentalRequestMapper requestMapper;
    private final RequestStatusPolicy statusPolicy;

    public RentalRequestServiceImpl(RentalRequestRepository requestRepository,
                                    UserRepository userRepository,
                                    LocationService locationService,
                                    CurrentUserProvider currentUserProvider,
                                    OwnershipVerifier ownershipVerifier,
                                    RentalRequestMapper requestMapper,
                                    RequestStatusPolicy statusPolicy) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.locationService = locationService;
        this.currentUserProvider = currentUserProvider;
        this.ownershipVerifier = ownershipVerifier;
        this.requestMapper = requestMapper;
        this.statusPolicy = statusPolicy;
    }

    // ------------------------------------------------------------------ create
    @Override
    @Transactional
    public RentalRequestDto create(CreateRentalRequestDto dto) {
        validateZoneFields(dto.googleMapsUrl(), dto.radiusKm());

        User tenant = getCurrentUser();

        RentalRequest.RentalRequestBuilder builder = RentalRequest.builder()
                .tenant(tenant)
                .title(dto.title().trim())
                .description(dto.description().trim())
                .desiredType(dto.desiredType())
                .budgetMin(dto.budgetMin())
                .budgetMax(dto.budgetMax())
                .currency(dto.currency())
                .city(dto.city().trim())
                .area(dto.area() != null ? dto.area().trim() : null)
                .rooms(dto.rooms())
                .desiredFrom(dto.desiredFrom())
                .status(RequestStatus.ACTIVE);

        applyZone(builder, dto.googleMapsUrl(), dto.radiusKm());

        RentalRequest saved = requestRepository.save(builder.build());
        return requestMapper.toDto(saved);
    }

    // ------------------------------------------------------------------ update
    @Override
    @Transactional
    public RentalRequestDto update(Long id, UpdateRentalRequestDto dto) {
        validateZoneFields(dto.googleMapsUrl(), dto.radiusKm());

        RentalRequest request = findOwned(id);

        request.setTitle(dto.title().trim());
        request.setDescription(dto.description().trim());
        request.setDesiredType(dto.desiredType());
        request.setBudgetMin(dto.budgetMin());
        request.setBudgetMax(dto.budgetMax());
        request.setCurrency(dto.currency());
        request.setCity(dto.city().trim());
        request.setArea(dto.area() != null ? dto.area().trim() : null);
        request.setRooms(dto.rooms());
        request.setDesiredFrom(dto.desiredFrom());

        // Resetăm zona și reaplică
        request.setGoogleMapsUrl(null);
        request.setCenterLatitude(null);
        request.setCenterLongitude(null);
        request.setRadiusKm(null);

        if (dto.googleMapsUrl() != null && !dto.googleMapsUrl().isBlank()) {
            Coordinates coords = locationService.parse(dto.googleMapsUrl());
            request.setGoogleMapsUrl(dto.googleMapsUrl().trim());
            request.setCenterLatitude(coords.latitude());
            request.setCenterLongitude(coords.longitude());
            request.setRadiusKm(dto.radiusKm());
        }

        RentalRequest saved = requestRepository.save(request);
        return requestMapper.toDto(saved);
    }

    // ------------------------------------------------------------------ getOwned
    @Override
    @Transactional(readOnly = true)
    public RentalRequestDto getOwned(Long id) {
        return requestMapper.toDto(findOwned(id));
    }

    // ------------------------------------------------------------------ listOwned
    @Override
    @Transactional(readOnly = true)
    public PageResponse<RentalRequestSummaryDto> listOwned(RequestStatus status, Pageable pageable) {
        Long tenantId = getCurrentUserId();

        Pageable sorted = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt", "id")
                );

        Page<RentalRequest> page;
        if (status == null || status == RequestStatus.DELETED) {
            page = requestRepository.findByTenantId(tenantId, sorted);
        } else {
            page = requestRepository.findByTenantIdAndStatus(tenantId, status, sorted);
        }

        return PageResponse.from(page.map(requestMapper::toSummaryDto));
    }

    // ------------------------------------------------------------------ changeStatus
    @Override
    @Transactional
    public RentalRequestDto changeStatus(Long id, ChangeRequestStatusDto dto) {
        RentalRequest request = findOwned(id);
        RequestStatus newStatus = dto.status();

        if (!statusPolicy.isAllowed(request.getStatus(), newStatus)) {
            throw new ConflictException(
                    "Schimbarea statusului din " + request.getStatus() + " în " + newStatus + " nu este permisă."
            );
        }

        request.setStatus(newStatus);
        return requestMapper.toDto(requestRepository.save(request));
    }

    // ------------------------------------------------------------------ delete
    @Override
    @Transactional
    public void delete(Long id) {
        RentalRequest request = findOwned(id);
        request.setStatus(RequestStatus.DELETED);
        requestRepository.save(request);
    }

    // ------------------------------------------------------------------ helpers
    private RentalRequest findOwned(Long id) {
        Long currentUserId = getCurrentUserId();
        RentalRequest request = requestRepository.findById(id)
                .filter(r -> r.getStatus() != RequestStatus.DELETED)
                .orElseThrow(() -> new NotFoundException("Cererea nu a fost găsită."));
        ownershipVerifier.verify(request, currentUserId);
        return request;
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

    /**
     * Validează că googleMapsUrl și radiusKm sunt ori ambele prezente ori ambele absente.
     * Dacă e prezent link-ul, raza trebuie să fie între 1 și 50 km (CER-03).
     */
    private void validateZoneFields(String googleMapsUrl, BigDecimal radiusKm) {
        boolean hasUrl = googleMapsUrl != null && !googleMapsUrl.isBlank();
        boolean hasRadius = radiusKm != null;

        if (hasUrl && !hasRadius) {
            throw new BadRequestException("radiusKm este obligatoriu când se furnizează un link Google Maps.");
        }
        if (!hasUrl && hasRadius) {
            throw new BadRequestException("googleMapsUrl este obligatoriu când se furnizează raza.");
        }
        if (hasUrl) {
            if (radiusKm.compareTo(BigDecimal.ONE) < 0 || radiusKm.compareTo(new BigDecimal("50")) > 0) {
                throw new BadRequestException("Raza trebuie să fie între 1 și 50 km.");
            }
        }
    }

    private void applyZone(RentalRequest.RentalRequestBuilder builder,
                           String googleMapsUrl, BigDecimal radiusKm) {
        if (googleMapsUrl != null && !googleMapsUrl.isBlank()) {
            Coordinates coords = locationService.parse(googleMapsUrl);
            builder.googleMapsUrl(googleMapsUrl.trim())
                   .centerLatitude(coords.latitude())
                   .centerLongitude(coords.longitude())
                   .radiusKm(radiusKm);
        }
    }
}
