package ro.rentalplatform.rentalrequest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ro.rentalplatform.rentalrequest.domain.RentalRequest;
import ro.rentalplatform.rentalrequest.dto.RentalRequestDetailsDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSummaryDto;
import ro.rentalplatform.user.domain.User;

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
     * Mapare sumară – pentru listele paginate GET /api/requests/mine și GET /api/requests.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "tenant", source = "tenant")
    @Mapping(target = "createdAt", source = "createdAt")
    RentalRequestSummaryDto toSummaryDto(RentalRequest request);

    /**
     * Detaliile cererii pentru proprietar – GET /api/requests/{id}.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "tenant", source = "tenant")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    RentalRequestDetailsDto toDetailsDto(RentalRequest request);

    RentalRequestSummaryDto.Tenant toSummaryTenant(User tenant);

    RentalRequestDetailsDto.Tenant toDetailsTenant(User tenant);
}
