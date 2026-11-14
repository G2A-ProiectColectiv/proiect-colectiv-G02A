package ro.rentalplatform.listing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.listing.controller.ListingSearchController;
import ro.rentalplatform.listing.domain.Amenity;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.listing.dto.ListingSearchCriteria;
import ro.rentalplatform.listing.service.ListingSearchService;
import ro.rentalplatform.listing.service.ListingSearchService.BoundingBox;
import ro.rentalplatform.security.CorsConfig;
import ro.rentalplatform.security.CurrentUserProvider;
import ro.rentalplatform.security.JwtService;
import ro.rentalplatform.security.RestAccessDeniedHandler;
import ro.rentalplatform.security.RestAuthenticationEntryPoint;
import ro.rentalplatform.security.SecurityConfig;
import ro.rentalplatform.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ListingSearchController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class, CorsConfig.class})
class ListingSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListingSearchService searchService;
    @MockitoBean
    private CurrentUserProvider currentUserProvider;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        when(searchService.search(any(), any())).thenReturn(new PageResponse<>(List.of(), 0, 12, 0, 0));
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.empty());
    }

    private ListingSearchCriteria capturedCriteria() {
        ArgumentCaptor<ListingSearchCriteria> captor = ArgumentCaptor.forClass(ListingSearchCriteria.class);
        verify(searchService).search(captor.capture(), any());
        return captor.getValue();
    }

    private Pageable capturedPageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(searchService).search(any(), captor.capture());
        return captor.getValue();
    }

    // ---- public access (CAUT-01) ----

    @Test
    void searchWorksWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/listings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());

        Pageable pageable = capturedPageable();
        assertEquals(12, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("createdAt").getDirection());
    }

    @Test
    void myListingsStillRequireLandlordRole() throws Exception {
        // the "mine" rule must stay before the public /api/listings/{id} rule
        mockMvc.perform(get("/api/listings/mine")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/listings/mine/5")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "TENANT")
    void myListingsAreForbiddenForTenants() throws Exception {
        mockMvc.perform(get("/api/listings/mine")).andExpect(status().isForbidden());
    }

    // ---- filters from the query string ----

    @Test
    void bindsAllFiltersFromQueryString() throws Exception {
        mockMvc.perform(get("/api/listings")
                .param("minPrice", "100")
                .param("maxPrice", "500")
                .param("type", "APARTMENT")
                .param("minRooms", "2")
                .param("minBathrooms", "1")
                .param("minArea", "40")
                .param("maxArea", "90")
                .param("city", "Cluj-Napoca")
                .param("amenities", "PARKING,BALCONY")
                .param("availableBy", "2026-12-31"))
            .andExpect(status().isOk());

        ListingSearchCriteria criteria = capturedCriteria();
        assertEquals(new BigDecimal("100"), criteria.minPrice());
        assertEquals(new BigDecimal("500"), criteria.maxPrice());
        assertEquals(PropertyType.APARTMENT, criteria.type());
        assertEquals((short) 2, criteria.minRooms());
        assertEquals((short) 1, criteria.minBathrooms());
        assertEquals(new BigDecimal("40"), criteria.minArea());
        assertEquals(new BigDecimal("90"), criteria.maxArea());
        assertEquals("Cluj-Napoca", criteria.city());
        assertEquals(Set.of(Amenity.PARKING, Amenity.BALCONY), criteria.amenities());
        assertEquals(LocalDate.of(2026, 12, 31), criteria.availableBy());
    }

    @Test
    void amenitiesCanAlsoBeRepeatedParameters() throws Exception {
        mockMvc.perform(get("/api/listings").param("amenities", "PARKING").param("amenities", "INTERNET"))
            .andExpect(status().isOk());

        assertEquals(Set.of(Amenity.PARKING, Amenity.INTERNET), capturedCriteria().amenities());
    }

    @Test
    void negativeOrUnknownFilterValuesReturn400() throws Exception {
        mockMvc.perform(get("/api/listings").param("minPrice", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'minPrice')]").exists());
        mockMvc.perform(get("/api/listings").param("type", "CASTLE"))
            .andExpect(status().isBadRequest());

        verify(searchService, never()).search(any(), any());
    }

    // ---- sorting and paging (1.5.1) ----

    @Test
    void sortsByAllowedFieldAndCapsPageSize() throws Exception {
        mockMvc.perform(get("/api/listings").param("sort", "price,asc").param("page", "3").param("size", "1000"))
            .andExpect(status().isOk());

        Pageable pageable = capturedPageable();
        assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("price").getDirection());
        assertEquals(3, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
    }

    @Test
    void sortingByInternalFieldReturns400() throws Exception {
        mockMvc.perform(get("/api/listings").param("sort", "landlord,asc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'sort')]").exists());
    }

    // ---- map (1.5.4) ----

    @Test
    void mapParsesBboxAndForwardsFilters() throws Exception {
        when(searchService.findForMap(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/listings/map").param("bbox", "44.33,25.96,44.55,26.25").param("type", "STUDIO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());

        ArgumentCaptor<ListingSearchCriteria> criteria = ArgumentCaptor.forClass(ListingSearchCriteria.class);
        verify(searchService).findForMap(criteria.capture(), eq(new BoundingBox(44.33, 25.96, 44.55, 26.25)));
        assertEquals(PropertyType.STUDIO, criteria.getValue().type());
    }

    @Test
    void mapWithoutValidBboxReturns400() throws Exception {
        mockMvc.perform(get("/api/listings/map"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'bbox')]").exists());
        mockMvc.perform(get("/api/listings/map").param("bbox", "1,2,3"))
            .andExpect(status().isBadRequest());
    }

    // ---- details (1.5.3) ----

    @Test
    void detailsForAnonymousViewerHidePhone() throws Exception {
        mockMvc.perform(get("/api/listings/5")).andExpect(status().isOk());

        verify(searchService).getPublicDetails(5L, false);
    }

    @Test
    @WithMockUser
    void detailsForLoggedInViewerIncludePhone() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.of(1L));

        mockMvc.perform(get("/api/listings/5")).andExpect(status().isOk());

        verify(searchService).getPublicDetails(5L, true);
    }

    @Test
    void unavailableListingReturns404() throws Exception {
        when(searchService.getPublicDetails(eq(5L), anyBoolean()))
                .thenThrow(new NotFoundException("Anunțul nu mai este disponibil."));

        mockMvc.perform(get("/api/listings/5"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Anunțul nu mai este disponibil."));
    }
}
