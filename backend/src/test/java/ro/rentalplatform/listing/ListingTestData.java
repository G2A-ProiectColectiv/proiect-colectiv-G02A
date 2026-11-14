package ro.rentalplatform.listing;

import org.springframework.test.util.ReflectionTestUtils;
import ro.rentalplatform.listing.domain.Amenity;
import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.Listing;
import ro.rentalplatform.listing.domain.ListingImage;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.listing.dto.CreateListingDto;
import ro.rentalplatform.listing.dto.ListingDto;
import ro.rentalplatform.listing.dto.UpdateListingDto;
import ro.rentalplatform.user.domain.Role;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Builders shared by the listing tests. Entities get their ids by reflection (the database sets them normally). */
final class ListingTestData {

    static final String MAPS_URL = "https://maps.app.goo.gl/AbCdEf123";
    static final LocalDate AVAILABLE_FROM = LocalDate.of(2026, 12, 1);

    private ListingTestData() {
    }

    static User landlord(Long id) {
        User user = User.builder()
                .email("landlord" + id + "@test.ro")
                .passwordHash("hashed")
                .firstName("Ion")
                .lastName("Popescu")
                .role(Role.LANDLORD)
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    static Listing listing(Long id, User owner, ListingStatus status) {
        Listing listing = Listing.builder()
                .landlord(owner)
                .title("Apartament 2 camere")
                .description("Luminos")
                .type(PropertyType.APARTMENT)
                .price(new BigDecimal("450"))
                .currency(Currency.EUR)
                .rooms((short) 2)
                .bathrooms((short) 1)
                .areaSqm(new BigDecimal("55"))
                .address("Str. Exemplu 1")
                .city("București")
                .latitude(44.0)
                .longitude(26.0)
                .googleMapsUrl(MAPS_URL)
                .availableFrom(AVAILABLE_FROM)
                .status(status)
                .build();
        ReflectionTestUtils.setField(listing, "id", id);
        return listing;
    }

    static ListingImage image(Long id, Listing listing, int position, boolean primary) {
        return ListingImage.builder()
                .id(id)
                .listing(listing)
                .url("listings/" + listing.getId() + "/img" + id + ".jpg")
                .position((short) position)
                .primary(primary)
                .build();
    }

    static CreateListingDto createDto(String googleMapsUrl, Set<Amenity> amenities) {
        return new CreateListingDto("  Apartament 2 camere ", " Luminos ", PropertyType.APARTMENT,
                new BigDecimal("450"), Currency.EUR, (short) 2, (short) 1, new BigDecimal("55"),
                " Str. Exemplu 1 ", " București ", amenities, AVAILABLE_FROM, " " + googleMapsUrl + " ");
    }

    static UpdateListingDto updateDto(String title, Set<Amenity> amenities) {
        return new UpdateListingDto(title, "Descriere nouă", PropertyType.STUDIO,
                new BigDecimal("300"), Currency.RON, (short) 1, (short) 1, new BigDecimal("30"),
                "Str. Nouă 2", "Cluj-Napoca", amenities, AVAILABLE_FROM, MAPS_URL);
    }

    static ListingDto dto(Long id, ListingStatus status) {
        return new ListingDto(id, "Apartament 2 camere", "Luminos", PropertyType.APARTMENT,
                new BigDecimal("450"), Currency.EUR, (short) 2, (short) 1, new BigDecimal("55"),
                "Str. Exemplu 1", "București", List.of(Amenity.BALCONY), AVAILABLE_FROM, MAPS_URL,
                44.4268, 26.1025, status, List.of(), null, null);
    }
}
