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
    public RentalRequestDetailsDto getDetails(Long id) {
        RentalRequest request = requestRepository.findById(id)
                .filter(candidate -> candidate.getStatus() == RequestStatus.ACTIVE)
                .filter(candidate -> candidate.getTenant() != null
                        && candidate.getTenant().getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Cererea nu mai este disponibilă."));
        return requestMapper.toDetailsDto(request);
    }
}
