package ro.rentalplatform.rentalrequest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ro.rentalplatform.rentalrequest.domain.RentalRequest;
import ro.rentalplatform.rentalrequest.dto.RentalRequestDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSummaryDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentalRequestMapper {

    /**
     * Mapare completă – pentru GET /api/requests/mine/{id}.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    RentalRequestDto toDto(RentalRequest request);

    /**
     * Mapare sumară – pentru lista paginată GET /api/requests/mine.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", source = "createdAt")
    RentalRequestSummaryDto toSummaryDto(RentalRequest request);
}
