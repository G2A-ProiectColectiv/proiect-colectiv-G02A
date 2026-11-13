package ro.rentalplatform.rentalrequest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.rentalplatform.common.domain.BaseEntity;
import ro.rentalplatform.common.domain.OwnedResource;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.user.domain.User;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "rental_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalRequest extends BaseEntity implements OwnedResource {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "desired_type", nullable = false, length = 20)
    private PropertyType desiredType;

    @Column(name = "budget_min", nullable = false, precision = 10, scale = 2)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", nullable = false, precision = 10, scale = 2)
    private BigDecimal budgetMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private ro.rentalplatform.listing.domain.Currency currency;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "area", length = 100)
    private String area;

    @Column(name = "rooms")
    private Short rooms;

    @Column(name = "desired_from", nullable = false)
    private LocalDate desiredFrom;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status;

    // --- Zona opțională ---

    @Column(name = "google_maps_url", length = 2000)
    private String googleMapsUrl;

    @Column(name = "center_latitude")
    private Double centerLatitude;

    @Column(name = "center_longitude")
    private Double centerLongitude;

    @Column(name = "radius_km", precision = 5, scale = 2)
    private BigDecimal radiusKm;

    @Override
    public Long getOwnerId() {
        return tenant != null ? tenant.getId() : null;
    }
}
