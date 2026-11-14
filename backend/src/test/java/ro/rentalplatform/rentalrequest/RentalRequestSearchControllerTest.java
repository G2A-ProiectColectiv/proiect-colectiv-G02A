package ro.rentalplatform.rentalrequest;

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
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.rentalrequest.controller.RentalRequestSearchController;
import ro.rentalplatform.rentalrequest.dto.RentalRequestSearchCriteria;
import ro.rentalplatform.rentalrequest.service.RentalRequestSearchService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RentalRequestSearchController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class, CorsConfig.class})
class RentalRequestSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RentalRequestSearchService searchService;
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

    @Test
    void bindsFiltersAndSort() throws Exception {
        mockMvc.perform(get("/api/requests")
                .param("type", "APARTMENT")
                .param("minBudget", "300")
                .param("maxBudget", "500")
                .param("city", "Cluj-Napoca")
                .param("area", "Mărăști")
                .param("minRooms", "2")
                .param("desiredFrom", "2027-01-01")
                .param("sort", "budgetMax,desc"))
            .andExpect(status().isOk());

        ArgumentCaptor<RentalRequestSearchCriteria> criteria = ArgumentCaptor.forClass(RentalRequestSearchCriteria.class);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(searchService).search(criteria.capture(), pageable.capture());
        assertEquals(PropertyType.APARTMENT, criteria.getValue().type());
        assertEquals(new BigDecimal("300"), criteria.getValue().minBudget());
        assertEquals(new BigDecimal("500"), criteria.getValue().maxBudget());
        assertEquals("Mărăști", criteria.getValue().area());
        assertEquals(LocalDate.of(2027, 1, 1), criteria.getValue().desiredFrom());
        assertEquals(Sort.Direction.DESC, pageable.getValue().getSort().getOrderFor("budgetMax").getDirection());
    }

    @Test
    void sortingByFieldOutsideTheListReturns400() throws Exception {
        mockMvc.perform(get("/api/requests").param("sort", "price,asc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'sort')]").exists());
    }

    @Test
    void myRequestsStillRequireTenantRole() throws Exception {
        // the "mine" rule must stay before the /api/requests/{id} rule
        mockMvc.perform(get("/api/requests/mine")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LANDLORD")
    void myRequestsAreForbiddenForLandlords() throws Exception {
        mockMvc.perform(get("/api/requests/mine")).andExpect(status().isForbidden());
    }

    @Test
    void detailsForAnonymousViewerHidePhone() throws Exception {
        mockMvc.perform(get("/api/requests/7")).andExpect(status().isOk());

        verify(searchService).getDetails(7L, false);
    }

    @Test
    @WithMockUser(roles = "LANDLORD")
    void detailsForLoggedInViewerIncludePhone() throws Exception {
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.of(1L));

        mockMvc.perform(get("/api/requests/7")).andExpect(status().isOk());

        verify(searchService).getDetails(7L, true);
    }
}
