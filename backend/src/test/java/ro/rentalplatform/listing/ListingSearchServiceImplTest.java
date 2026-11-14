package ro.rentalplatform.listing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ro.rentalplatform.AbstractIntegrationTest;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.listing.domain.Amenity;
import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.Listing;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.listing.dto.ListingDetailsDto;
import ro.rentalplatform.listing.dto.ListingMapMarkerDto;
import ro.rentalplatform.listing.dto.ListingSearchCriteria;
import ro.rentalplatform.listing.dto.ListingSummaryDto;
import ro.rentalplatform.listing.repository.ListingRepository;
import ro.rentalplatform.listing.service.ListingSearchService;
import ro.rentalplatform.listing.service.ListingSearchService.BoundingBox;
import ro.rentalplatform.user.domain.Role;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;
import ro.rentalplatform.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test on prepared data (1.5.2–1.5.4): the filters are SQL specifications,
 * so they are checked against the real PostgreSQL database.
 */
class ListingSearchServiceImplTest extends AbstractIntegrationTest {

    private static final BoundingBox BUCHAREST = new BoundingBox(44.33, 25.96, 44.55, 26.25);

    @Autowired
    private ListingSearchService searchService;
    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private UserRepository userRepository;

    private User landlord;
    private Listing studioMilitari;
    private Listing apartmentFloreasca;
    private Listing apartmentCluj;
    private Listing rented;
    private Listing ofBlockedLandlord;

    // ---- prepared data ----

    @BeforeEach
    void prepareData() {
        landlord = saveUser("ion@test.ro", UserStatus.ACTIVE, "0711111111");
        User blocked = saveUser("blocat@test.ro", UserStatus.BLOCKED, null);

        studioMilitari = save(listing(landlord, "Garsonieră Militari", PropertyType.STUDIO, "300", 1, 1, "30",
                "București", Set.of(Amenity.INTERNET), LocalDate.of(2026, 12, 1), 44.43, 26.03));
        apartmentFloreasca = save(listing(landlord, "Apartament Floreasca", PropertyType.APARTMENT, "600", 3, 2, "80",
                "București", Set.of(Amenity.PARKING, Amenity.BALCONY, Amenity.INTERNET), LocalDate.of(2027, 1, 15),
                44.46, 26.10));
        apartmentCluj = save(listing(landlord, "Apartament Cluj", PropertyType.APARTMENT, "450", 2, 1, "55",
                "Cluj-Napoca", Set.of(Amenity.PARKING), LocalDate.of(2026, 12, 1), 46.77, 23.62));

        Listing rentedListing = listing(landlord, "Închiriat", PropertyType.STUDIO, "300", 1, 1, "30",
                "București", Set.of(), LocalDate.of(2026, 12, 1), 44.44, 26.05);
        rentedListing.setStatus(ListingStatus.RENTED);
        rented = save(rentedListing);

        Listing deletedListing = listing(landlord, "Șters", PropertyType.STUDIO, "300", 1, 1, "30",
                "București", Set.of(), LocalDate.of(2026, 12, 1), 44.44, 26.05);
        deletedListing.setStatus(ListingStatus.DELETED);
        save(deletedListing);

        ofBlockedLandlord = save(listing(blocked, "Al unui cont blocat", PropertyType.STUDIO, "300", 1, 1, "30",
                "București", Set.of(), LocalDate.of(2026, 12, 1), 44.44, 26.05));
    }

    private User saveUser(String email, UserStatus status, String phone) {
        return userRepository.save(User.builder()
                .email(email)
                .passwordHash("hashed")
                .firstName("Ion")
                .lastName("Popescu")
                .phone(phone)
                .role(Role.LANDLORD)
                .status(status)
                .build());
    }

    private static Listing listing(User owner, String title, PropertyType type, String price, int rooms, int bathrooms,
                                   String area, String city, Set<Amenity> amenities, LocalDate availableFrom,
                                   double latitude, double longitude) {
        return Listing.builder()
                .landlord(owner)
                .title(title)
                .description("Descriere")
                .type(type)
                .price(new BigDecimal(price))
                .currency(Currency.EUR)
                .rooms((short) rooms)
                .bathrooms((short) bathrooms)
                .areaSqm(new BigDecimal(area))
                .address("Str. Exemplu 1")
                .city(city)
                .latitude(latitude)
                .longitude(longitude)
                .availableFrom(availableFrom)
                .status(ListingStatus.ACTIVE)
                .amenities(new LinkedHashSet<>(amenities))
                .build();
    }

    private Listing save(Listing listing) {
        return listingRepository.save(listing);
    }

    // ---- helpers ----

    private static ListingSearchCriteria criteria() {
        return new ListingSearchCriteria(null, null, null, null, null, null, null, null, null, null);
    }

    private Set<Long> search(ListingSearchCriteria criteria) {
        PageResponse<ListingSummaryDto> page = searchService.search(criteria, PageRequest.of(0, 50));
        Set<Long> ids = new LinkedHashSet<>();
        page.items().forEach(item -> ids.add(item.id()));
        return ids;
    }

    private static Set<Long> ids(Listing... listings) {
        Set<Long> ids = new LinkedHashSet<>();
        for (Listing listing : listings) {
            ids.add(listing.getId());
        }
        return ids;
    }

    // ---- always-on conditions (CAUT-05) ----

    @Test
    void withoutFiltersOnlyActiveListingsOfActiveLandlordsAppear() {
        // RENTED, DELETED and listings of blocked accounts never show up
        assertEquals(ids(studioMilitari, apartmentFloreasca, apartmentCluj), search(criteria()));
    }

    // ---- each filter alone ----

    @Test
    void filtersByPriceRange() {
        assertEquals(ids(apartmentFloreasca, apartmentCluj),
                search(new ListingSearchCriteria(new BigDecimal("400"), null, null, null, null, null, null, null, null, null)));
        assertEquals(ids(studioMilitari, apartmentCluj),
                search(new ListingSearchCriteria(null, new BigDecimal("450"), null, null, null, null, null, null, null, null)));
    }

    @Test
    void filtersByType() {
        assertEquals(ids(apartmentFloreasca, apartmentCluj),
                search(new ListingSearchCriteria(null, null, PropertyType.APARTMENT, null, null, null, null, null, null, null)));
    }

    @Test
    void filtersByMinimumRoomsAndBathrooms() {
        assertEquals(ids(apartmentFloreasca, apartmentCluj),
                search(new ListingSearchCriteria(null, null, null, (short) 2, null, null, null, null, null, null)));
        assertEquals(ids(apartmentFloreasca),
                search(new ListingSearchCriteria(null, null, null, null, (short) 2, null, null, null, null, null)));
    }

    @Test
    void filtersByAreaRange() {
        assertEquals(ids(apartmentFloreasca, apartmentCluj),
                search(new ListingSearchCriteria(null, null, null, null, null, new BigDecimal("50"), null, null, null, null)));
        assertEquals(ids(studioMilitari, apartmentCluj),
                search(new ListingSearchCriteria(null, null, null, null, null, null, new BigDecimal("60"), null, null, null)));
    }

    @Test
    void cityIgnoresCaseDiacriticsAndDashes() {
        assertEquals(ids(studioMilitari, apartmentFloreasca),
                search(new ListingSearchCriteria(null, null, null, null, null, null, null, "bucuresti", null, null)));
        assertEquals(ids(apartmentCluj),
                search(new ListingSearchCriteria(null, null, null, null, null, null, null, "CLUJ napoca", null, null)));
    }

    @Test
    void amenitiesMustAllBePresent() {
        assertEquals(ids(apartmentFloreasca, apartmentCluj),
                search(new ListingSearchCriteria(null, null, null, null, null, null, null, null,
                        Set.of(Amenity.PARKING), null)));
        assertEquals(ids(apartmentFloreasca),
                search(new ListingSearchCriteria(null, null, null, null, null, null, null, null,
                        Set.of(Amenity.PARKING, Amenity.BALCONY), null)));
    }

    @Test
    void filtersByAvailabilityDate() {
        assertEquals(ids(studioMilitari, apartmentCluj),
                search(new ListingSearchCriteria(null, null, null, null, null, null, null, null, null,
                        LocalDate.of(2026, 12, 31))));
    }

    // ---- combined filters ----

    @Test
    void combinedFiltersMustAllMatch() {
        assertEquals(ids(apartmentCluj),
                search(new ListingSearchCriteria(null, new BigDecimal("500"), PropertyType.APARTMENT, null, null,
                        null, null, "Cluj-Napoca", Set.of(Amenity.PARKING), null)));
        assertEquals(Set.of(),
                search(new ListingSearchCriteria(new BigDecimal("1000"), null, PropertyType.APARTMENT, null, null,
                        null, null, "București", null, null)));
    }

    // ---- sorting and paging ----

    @Test
    void sortsByPriceAndPages() {
        PageResponse<ListingSummaryDto> firstPage = searchService.search(criteria(),
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "price", "id")));

        assertEquals(List.of(studioMilitari.getId(), apartmentCluj.getId()),
                firstPage.items().stream().map(ListingSummaryDto::id).toList());
        assertEquals(3, firstPage.totalElements());
        assertEquals(2, firstPage.totalPages());
    }

    // ---- public details (1.5.3) ----

    @Test
    void detailsShowOwnerPhoneOnlyToAuthenticatedViewers() {
        // DET-03
        ListingDetailsDto anonymous = searchService.getPublicDetails(apartmentCluj.getId(), false);
        ListingDetailsDto authenticated = searchService.getPublicDetails(apartmentCluj.getId(), true);

        assertNull(anonymous.owner().phone());
        assertEquals("0711111111", authenticated.owner().phone());
        assertEquals("Ion", anonymous.owner().firstName());
        assertEquals(List.of(Amenity.PARKING), anonymous.amenities());
    }

    @Test
    void detailsOfUnavailableListingsAreNotFound() {
        // DET-04
        assertThrows(NotFoundException.class, () -> searchService.getPublicDetails(rented.getId(), true));
        assertThrows(NotFoundException.class, () -> searchService.getPublicDetails(ofBlockedLandlord.getId(), true));
        assertThrows(NotFoundException.class, () -> searchService.getPublicDetails(999_999L, true));
    }

    // ---- map (1.5.4) ----

    @Test
    void mapReturnsOnlyListingsInsideTheVisibleArea() {
        List<ListingMapMarkerDto> markers = searchService.findForMap(criteria(), BUCHAREST);

        Set<Long> markerIds = new LinkedHashSet<>();
        markers.forEach(marker -> markerIds.add(marker.id()));
        assertEquals(Set.of(studioMilitari.getId(), apartmentFloreasca.getId()), markerIds);
    }

    @Test
    void mapAppliesTheSameFiltersAsTheList() {
        // HARTA-03
        List<ListingMapMarkerDto> markers = searchService.findForMap(
                new ListingSearchCriteria(null, null, PropertyType.STUDIO, null, null, null, null, null, null, null),
                BUCHAREST);

        assertEquals(1, markers.size());
        assertEquals(studioMilitari.getId(), markers.get(0).id());
        assertEquals(44.43, markers.get(0).latitude());
    }

    @Test
    void mapReturnsAtMost500Markers() {
        // HARTA-04
        List<Listing> many = new ArrayList<>();
        for (int i = 0; i < ListingSearchService.MAX_MAP_MARKERS + 10; i++) {
            many.add(listing(landlord, "Anunț " + i, PropertyType.ROOM, "200", 1, 1, "15",
                    "București", Set.of(), LocalDate.of(2026, 12, 1), 44.40 + i * 0.0001, 26.10));
        }
        listingRepository.saveAll(many);

        assertEquals(ListingSearchService.MAX_MAP_MARKERS, searchService.findForMap(criteria(), BUCHAREST).size());
    }
}
