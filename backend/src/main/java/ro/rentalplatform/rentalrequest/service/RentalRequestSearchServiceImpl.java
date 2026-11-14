package ro.rentalplatform.rentalrequest.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.rentalrequest.domain.RentalRequest;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;
import ro.rentalplatform.rentalrequest.dto.RentalRequestDetailsDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSearchCriteria;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSummaryDto;
import ro.rentalplatform.rentalrequest.mapper.RentalRequestMapper;
import ro.rentalplatform.rentalrequest.repository.RentalRequestRepository;
import ro.rentalplatform.rentalrequest.repository.RentalRequestSpecifications;
import ro.rentalplatform.user.domain.UserStatus;

@Service
public class RentalRequestSearchServiceImpl implements RentalRequestSearchService {

    private final RentalRequestRepository requestRepository;
    private final RentalRequestMapper requestMapper;

    public RentalRequestSearchServiceImpl(RentalRequestRepository requestRepository,
                                          RentalRequestMapper requestMapper) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RentalRequestSummaryDto> search(RentalRequestSearchCriteria criteria, Pageable pageable) {
        Specification<RentalRequest> spec = RentalRequestSpecifications.fromCriteria(criteria);
        return PageResponse.from(requestRepository.findAll(spec, pageable).map(requestMapper::toSummaryDto));
    }

    @Override
    @Transactional(readOnly = true)
    public RentalRequestDetailsDto getDetails(Long id, boolean viewerAuthenticated) {
        RentalRequest request = requestRepository.findById(id)
                .filter(candidate -> candidate.getStatus() == RequestStatus.ACTIVE)
                .filter(candidate -> candidate.getTenant() != null
                        && candidate.getTenant().getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Cererea nu mai este disponibilă."));

        RentalRequestDetailsDto details = requestMapper.toDetailsDto(request);
        return viewerAuthenticated ? details : withoutTenantPhone(details);
    }

    /** Vizitatorii neautentificați nu văd telefonul chiriașului. */
    private static RentalRequestDetailsDto withoutTenantPhone(RentalRequestDetailsDto details) {
        RentalRequestDetailsDto.Tenant tenant = details.tenant();
        RentalRequestDetailsDto.Tenant hidden = tenant == null ? null : new RentalRequestDetailsDto.Tenant(
                tenant.id(), tenant.firstName(), tenant.lastName(), tenant.avatarUrl(), null);

        return new RentalRequestDetailsDto(
                details.id(), details.title(), details.description(), details.desiredType(),
                details.budgetMin(), details.budgetMax(), details.currency(), details.city(),
                details.area(), details.rooms(), details.desiredFrom(), details.status(),
                details.googleMapsUrl(), details.centerLatitude(), details.centerLongitude(),
                details.radiusKm(), hidden, details.createdAt(), details.updatedAt());
    }
}
