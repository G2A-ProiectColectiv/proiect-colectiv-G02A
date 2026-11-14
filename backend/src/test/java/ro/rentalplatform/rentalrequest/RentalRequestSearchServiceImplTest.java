package ro.rentalplatform.rentalrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ro.rentalplatform.AbstractIntegrationTest;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.rentalrequest.domain.RentalRequest;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;
import ro.rentalplatform.rentalrequest.dto.RentalRequestDetailsDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSearchCriteria;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSummaryDto;
import ro.rentalplatform.rentalrequest.repository.RentalRequestRepository;
import ro.rentalplatform.rentalrequest.service.RentalRequestSearchService;
import ro.rentalplatform.user.domain.Role;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;
import ro.rentalplatform.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Integration test on prepared data (1.5.5): the filters are SQL specifications run against PostgreSQL. */
class RentalRequestSearchServiceImplTest extends AbstractIntegrationTest {

    @Autowired
    private RentalRequestSearchService searchService;
    @Autowired
    private RentalRequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;

    private RentalRequest studioMilitari;
    private RentalRequest apartmentMarasti;
    private RentalRequest bigApartment;
    private RentalRequest found;
    private RentalRequest ofBlockedTenant;

    // ---- prepared data ----

    @BeforeEach
    void prepareData() {
        User tenant = saveUser("ana@test.ro", UserStatus.ACTIVE, "0722222222");
        User blocked = saveUser("blocat@test.ro", UserStatus.BLOCKED, null);

        studioMilitari = save(request(tenant, PropertyType.STUDIO, "200", "350", "București",
                "Militari, lângă metrou", 1, LocalDate.of(2027, 1, 1)));
        apartmentMarasti = save(request(tenant, PropertyType.APARTMENT, "400", "600", "Cluj-Napoca",
                "Mărăști, lângă parc", 2, LocalDate.of(2027, 2, 1)));
        bigApartment = save(request(tenant, PropertyType.APARTMENT, "500", "800", "București",
                null, 3, LocalDate.of(2026, 12, 15)));

        RentalRequest foundRequest = request(tenant, PropertyType.STUDIO, "200", "350", "București",
                null, 1, LocalDate.of(2027, 1, 1));
        foundRequest.setStatus(RequestStatus.FOUND);
        found = save(foundRequest);

        RentalRequest deletedRequest = request(tenant, PropertyType.STUDIO, "200", "350", "București",
                null, 1, LocalDate.of(2027, 1, 1));
        deletedRequest.setStatus(RequestStatus.DELETED);
        save(deletedRequest);

        ofBlockedTenant = save(request(blocked, PropertyType.STUDIO, "200", "350", "București",
                null, 1, LocalDate.of(2027, 1, 1)));
    }

    private User saveUser(String email, UserStatus status, String phone) {
        return userRepository.save(User.builder()
                .email(email)
                .passwordHash("hashed")
                .firstName("Ana")
                .lastName("Pop")
                .phone(phone)
                .role(Role.TENANT)
                .status(status)
                .build());
    }

    private static RentalRequest request(User tenant, PropertyType type, String budgetMin, String budgetMax,
                                         String city, String area, int rooms, LocalDate desiredFrom) {
        return RentalRequest.builder()
                .tenant(tenant)
                .title("Caut locuință")
                .description("Descriere")
                .desiredType(type)
                .budgetMin(new BigDecimal(budgetMin))
                .budgetMax(new BigDecimal(budgetMax))
                .currency(Currency.EUR)
                .city(city)
                .area(area)
                .rooms((short) rooms)
                .desiredFrom(desiredFrom)
                .status(RequestStatus.ACTIVE)
                .build();
    }

    private RentalRequest save(RentalRequest request) {
        return requestRepository.save(request);
    }

    // ---- helpers ----

    private static RentalRequestSearchCriteria criteria() {
        return new RentalRequestSearchCriteria(null, null, null, null, null, null, null);
    }

    private static RentalRequestSearchCriteria budget(String min, String max) {
        return new RentalRequestSearchCriteria(null,
                min == null ? null : new BigDecimal(min), max == null ? null : new BigDecimal(max),
                null, null, null, null);
    }

    private Set<Long> search(RentalRequestSearchCriteria criteria) {
        PageResponse<RentalRequestSummaryDto> page = searchService.search(criteria, PageRequest.of(0, 50));
        Set<Long> ids = new LinkedHashSet<>();
        page.items().forEach(item -> ids.add(item.id()));
        return ids;
    }

    private static Set<Long> ids(RentalRequest... requests) {
        Set<Long> ids = new LinkedHashSet<>();
        for (RentalRequest request : requests) {
            ids.add(request.getId());
        }
        return ids;
    }

    // ---- always-on conditions ----

    @Test
    void withoutFiltersOnlyActiveRequestsOfActiveTenantsAppear() {
        // FOUND, DELETED and requests of blocked accounts never show up
        assertEquals(ids(studioMilitari, apartmentMarasti, bigApartment), search(criteria()));
    }

    // ---- budget: the request's range must overlap the searched range ----

    @Test
    void budgetRangeMatchesOverlappingRequests() {
        // searched 380–450: 200–350 is below, 500–800 is above, 400–600 overlaps
        assertEquals(ids(apartmentMarasti), search(budget("380", "450")));
    }

    @Test
    void budgetRangeTouchingTheEdgeCounts() {
        // 200–350 ends exactly where the searched range starts
        assertEquals(ids(studioMilitari, apartmentMarasti, bigApartment), search(budget("350", null)));
        // 500–800 starts exactly where the searched range ends
        assertEquals(ids(studioMilitari, apartmentMarasti, bigApartment), search(budget(null, "500")));
    }

    @Test
    void oneSidedBudgetFilters() {
        assertEquals(ids(apartmentMarasti, bigApartment), search(budget("550", null)));
        assertEquals(ids(studioMilitari), search(budget(null, "300")));
    }

    // ---- other filters ----

    @Test
    void filtersByType() {
        assertEquals(ids(apartmentMarasti, bigApartment),
                search(new RentalRequestSearchCriteria(PropertyType.APARTMENT, null, null, null, null, null, null)));
    }

    @Test
    void cityIgnoresCaseDiacriticsAndDashes() {
        assertEquals(ids(apartmentMarasti),
                search(new RentalRequestSearchCriteria(null, null, null, "cluj napoca", null, null, null)));
        assertEquals(ids(studioMilitari, bigApartment),
                search(new RentalRequestSearchCriteria(null, null, null, "BUCURESTI", null, null, null)));
    }

    @Test
    void areaMatchesPartOfTheTextWithoutDiacritics() {
        assertEquals(ids(apartmentMarasti),
                search(new RentalRequestSearchCriteria(null, null, null, null, "marasti", null, null)));
        assertEquals(ids(studioMilitari),
                search(new RentalRequestSearchCriteria(null, null, null, null, "metrou", null, null)));
    }

    @Test
    void filtersByMinimumRooms() {
        assertEquals(ids(apartmentMarasti, bigApartment),
                search(new RentalRequestSearchCriteria(null, null, null, null, null, (short) 2, null)));
    }

    @Test
    void filtersByStartDate() {
        // requests that start on or after the date the landlord's home is free
        assertEquals(ids(studioMilitari, apartmentMarasti),
                search(new RentalRequestSearchCriteria(null, null, null, null, null, null, LocalDate.of(2027, 1, 1))));
    }

    @Test
    void combinedFiltersMustAllMatch() {
        assertEquals(ids(bigApartment),
                search(new RentalRequestSearchCriteria(PropertyType.APARTMENT, new BigDecimal("700"), null,
                        "București", null, (short) 3, null)));
    }

    @Test
    void sortsByBudget() {
        PageResponse<RentalRequestSummaryDto> page = searchService.search(criteria(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "budgetMax", "id")));

        assertEquals(List.of(bigApartment.getId(), apartmentMarasti.getId(), studioMilitari.getId()),
                page.items().stream().map(RentalRequestSummaryDto::id).toList());
    }

    // ---- details ----

    @Test
    void detailsShowTenantPhoneOnlyToAuthenticatedViewers() {
        RentalRequestDetailsDto anonymous = searchService.getDetails(apartmentMarasti.getId(), false);
        RentalRequestDetailsDto authenticated = searchService.getDetails(apartmentMarasti.getId(), true);

        assertNull(anonymous.tenant().phone());
        assertEquals("0722222222", authenticated.tenant().phone());
        assertEquals("Ana", anonymous.tenant().firstName());
        assertEquals("Mărăști, lângă parc", anonymous.area());
    }

    @Test
    void detailsOfUnavailableRequestsAreNotFound() {
        assertThrows(NotFoundException.class, () -> searchService.getDetails(found.getId(), true));
        assertThrows(NotFoundException.class, () -> searchService.getDetails(ofBlockedTenant.getId(), true));
        assertThrows(NotFoundException.class, () -> searchService.getDetails(999_999L, true));
    }
}
