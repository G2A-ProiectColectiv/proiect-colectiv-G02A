package ro.rentalplatform.rentalrequest.repository;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.rentalrequest.domain.RentalRequest;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSearchCriteria;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Câte un {@link Specification} pentru fiecare filtru al căutării de cereri.
 * Un filtru fără valoare întoarce {@code null} și este ignorat la combinare.
 */
public final class RentalRequestSpecifications {

    private RentalRequestSpecifications() {
    }

    /** Combină filtrele din criterii, plus condițiile care se aplică mereu (CAUT-05). */
    public static Specification<RentalRequest> fromCriteria(RentalRequestSearchCriteria criteria) {
        List<Specification<RentalRequest>> specs = new ArrayList<>();
        specs.add(isActive());
        specs.add(tenantIsActive());
        if (criteria != null) {
            specs.add(hasType(criteria.type()));
            specs.add(budgetAtLeast(criteria.minBudget()));
            specs.add(budgetAtMost(criteria.maxBudget()));
            specs.add(inCity(criteria.city()));
            specs.add(inArea(criteria.area()));
            specs.add(minRooms(criteria.minRooms()));
            specs.add(desiredFromOnOrAfter(criteria.desiredFrom()));
        }
        return Specification.allOf(specs.stream().filter(Objects::nonNull).toList());
    }

    public static Specification<RentalRequest> isActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), RequestStatus.ACTIVE);
    }

    public static Specification<RentalRequest> tenantIsActive() {
        return (root, query, cb) -> {
            Join<RentalRequest, User> tenant = root.join("tenant");
            return cb.equal(tenant.get("status"), UserStatus.ACTIVE);
        };
    }

    public static Specification<RentalRequest> hasType(PropertyType type) {
        if (type == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("desiredType"), type);
    }

    /**
     * Capătul de jos al intervalului căutat: bugetul maxim al cererii trebuie să ajungă cel puțin aici.
     * Împreună cu {@link #budgetAtMost}, o cerere apare dacă intervalul ei se suprapune cu cel căutat.
     */
    public static Specification<RentalRequest> budgetAtLeast(BigDecimal minBudget) {
        if (minBudget == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("budgetMax"), minBudget);
    }

    /** Capătul de sus al intervalului căutat: bugetul minim al cererii nu trebuie să-l depășească. */
    public static Specification<RentalRequest> budgetAtMost(BigDecimal maxBudget) {
        if (maxBudget == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("budgetMin"), maxBudget);
    }

    /** Orașul se compară fără să conteze literele mari și mici. */
    public static Specification<RentalRequest> inCity(String city) {
        if (city == null || city.isBlank()) {
            return null;
        }
        String normalized = city.trim().toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(root.get("city")), normalized);
    }

    /** Zona conține textul căutat (de ex. „mărăști” găsește „Mărăști, lângă parc”). */
    public static Specification<RentalRequest> inArea(String area) {
        if (area == null || area.isBlank()) {
            return null;
        }
        String pattern = "%" + escapeLike(area.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("area")), pattern, '\\');
    }

    public static Specification<RentalRequest> minRooms(Short value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("rooms"), value);
    }

    public static Specification<RentalRequest> desiredFromOnOrAfter(LocalDate date) {
        if (date == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("desiredFrom"), date);
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
