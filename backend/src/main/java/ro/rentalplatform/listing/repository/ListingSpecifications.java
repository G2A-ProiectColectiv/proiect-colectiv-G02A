package ro.rentalplatform.listing.repository;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ro.rentalplatform.common.search.TextSearch;
import ro.rentalplatform.listing.domain.Amenity;
import ro.rentalplatform.listing.domain.Listing;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.listing.dto.ListingSearchCriteria;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Câte un {@link Specification} pentru fiecare filtru al căutării de anunțuri.
 * Un filtru fără valoare întoarce {@code null} și este ignorat la combinare.
 */
public final class ListingSpecifications {

    private ListingSpecifications() {
    }

    /** Combină toate filtrele din criterii, plus condițiile care se aplică mereu (CAUT-05). */
    public static Specification<Listing> fromCriteria(ListingSearchCriteria criteria) {
        List<Specification<Listing>> specs = new ArrayList<>();
        specs.add(isActive());
        specs.add(ownerIsActive());
        if (criteria != null) {
            specs.add(minPrice(criteria.minPrice()));
            specs.add(maxPrice(criteria.maxPrice()));
            specs.add(hasType(criteria.type()));
            specs.add(minRooms(criteria.minRooms()));
            specs.add(minBathrooms(criteria.minBathrooms()));
            specs.add(minArea(criteria.minArea()));
            specs.add(maxArea(criteria.maxArea()));
            specs.add(inCity(criteria.city()));
            specs.add(hasAllAmenities(criteria.amenities()));
            specs.add(availableBy(criteria.availableBy()));
        }
        return allOf(specs);
    }

    /** Ca {@link Specification#allOf}, dar sare peste filtrele {@code null}. */
    public static Specification<Listing> allOf(Collection<Specification<Listing>> specs) {
        return Specification.allOf(specs.stream().filter(Objects::nonNull).toList());
    }

    public static Specification<Listing> isActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), ListingStatus.ACTIVE);
    }

    public static Specification<Listing> ownerIsActive() {
        return (root, query, cb) -> {
            Join<Listing, User> landlord = root.join("landlord");
            return cb.equal(landlord.get("status"), UserStatus.ACTIVE);
        };
    }

    public static Specification<Listing> minPrice(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), value);
    }

    public static Specification<Listing> maxPrice(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), value);
    }

    public static Specification<Listing> hasType(PropertyType type) {
        if (type == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Listing> minRooms(Short value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("rooms"), value);
    }

    public static Specification<Listing> minBathrooms(Short value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("bathrooms"), value);
    }

    public static Specification<Listing> minArea(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("areaSqm"), value);
    }

    public static Specification<Listing> maxArea(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("areaSqm"), value);
    }

    /** Orașul se compară fără să conteze literele mari și mici, diacriticile sau „-” (vezi {@link TextSearch}). */
    public static Specification<Listing> inCity(String city) {
        String normalized = TextSearch.normalize(city);
        if (normalized.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(TextSearch.normalized(cb, root.get("city")), normalized);
    }

    /** Anunțul trebuie să aibă toate dotările cerute. */
    public static Specification<Listing> hasAllAmenities(Set<Amenity> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> {
            Expression<Collection<Amenity>> listingAmenities = root.get("amenities");
            Predicate[] predicates = amenities.stream()
                    .filter(Objects::nonNull)
                    .map(amenity -> cb.isMember(amenity, listingAmenities))
                    .toArray(Predicate[]::new);
            return cb.and(predicates);
        };
    }

    /** Disponibil cel târziu la data dată. */
    public static Specification<Listing> availableBy(LocalDate date) {
        if (date == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("availableFrom"), date);
    }

    /** Anunțurile din dreptunghiul vizibil pe hartă (HARTA-04). */
    public static Specification<Listing> inBoundingBox(double south, double west, double north, double east) {
        return (root, query, cb) -> {
            Predicate latitude = cb.between(root.get("latitude"), south, north);
            Predicate longitude = west <= east
                    ? cb.between(root.get("longitude"), west, east)
                    // dreptunghiul trece peste meridianul de 180°
                    : cb.or(
                            cb.greaterThanOrEqualTo(root.get("longitude"), west),
                            cb.lessThanOrEqualTo(root.get("longitude"), east)
                    );
            return cb.and(latitude, longitude);
        };
    }
}
