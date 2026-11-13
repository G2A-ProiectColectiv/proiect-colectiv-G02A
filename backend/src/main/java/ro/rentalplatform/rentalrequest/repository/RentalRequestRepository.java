package ro.rentalplatform.rentalrequest.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.rentalplatform.rentalrequest.domain.RentalRequest;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;

import java.util.List;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long>,
        JpaSpecificationExecutor<RentalRequest> {

    Page<RentalRequest> findByTenantIdAndStatus(Long tenantId, RequestStatus status, Pageable pageable);

    Page<RentalRequest> findByTenantId(Long tenantId, Pageable pageable);

    List<RentalRequest> findByTenantId(Long tenantId);

    @Modifying
    @Query("UPDATE RentalRequest r SET r.status = :status WHERE r.tenant.id = :tenantId AND r.status <> ro.rentalplatform.rentalrequest.domain.RequestStatus.DELETED")
    void markAllAsDeletedForTenant(@Param("tenantId") Long tenantId, @Param("status") RequestStatus status);
}
