package ro.rentalplatform.listing;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ro.rentalplatform.common.dto.PageResponse;
import ro.rentalplatform.common.exception.ConflictException;
import ro.rentalplatform.common.exception.ForbiddenOperationException;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.listing.controller.MyListingsController;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.service.ListingService;
import ro.rentalplatform.location.exception.CoordinatesNotFoundException;
import ro.rentalplatform.security.CorsConfig;
import ro.rentalplatform.security.JwtService;
import ro.rentalplatform.security.RestAccessDeniedHandler;
import ro.rentalplatform.security.RestAuthenticationEntryPoint;
import ro.rentalplatform.security.SecurityConfig;
import ro.rentalplatform.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyListingsController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class, CorsConfig.class})
class MyListingsControllerTest {

    private static final String VALID_BODY = """
        {
            "title": "Apartament 2 camere",
            "description": "Luminos",
            "type": "APARTMENT",
            "price": 450,
            "currency": "EUR",
            "rooms": 2,
            "bathrooms": 1,
            "areaSqm": 55,
            "address": "Str. Exemplu 1",
            "city": "București",
            "amenities": ["BALCONY"],
            "availableFrom": "2026-12-01",
            "googleMapsUrl": "https://maps.app.goo.gl/AbCdEf123"
        }
        """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListingService listingService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;

    // ---- roles (ANUN-01) ----

    @Test
    void withoutLoginReturns401() throws Exception {
        mockMvc.perform(get("/api/listings/mine"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "TENANT")
    void tenantCannotCreateListings() throws Exception {
        mockMvc.perform(post("/api/listings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isForbidden());

        verify(listingService, never()).create(any());
    }

    // ---- create ----

    @Test
    @WithMockUser(roles = "LANDLORD")
    void landlordCreatesListing() throws Exception {
        when(listingService.create(any())).thenReturn(ListingTestData.dto(10L, ListingStatus.ACTIVE));

        mockMvc.perform(post("/api/listings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.latitude").value(44.4268))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "LANDLORD")
    void negativePriceAndMissingFieldsReturn400() throws Exception {
        // ANUN-04
        mockMvc.perform(post("/api/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Apartament",
                        "description": "Luminos",
                        "type": "APARTMENT",
                        "price": -5,
                        "currency": "EUR",
                        "rooms": -1,
                        "bathrooms": 1,
                        "areaSqm": 0,
                        "address": "Str. Exemplu 1",
                        "city": "București",
                        "googleMapsUrl": "https://maps.app.goo.gl/AbCdEf123"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'price')]").exists())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'rooms')]").exists())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'areaSqm')]").exists())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'availableFrom')]").exists());

        verify(listingService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "LANDLORD")
    void linkWithoutCoordinatesReturns422() throws Exception {
        when(listingService.create(any())).thenThrow(new CoordinatesNotFoundException());

        mockMvc.perform(post("/api/listings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.message").value(CoordinatesNotFoundException.DEFAULT_MESSAGE));
    }

    // ---- update / get ----

    @Test
    @WithMockUser(roles = "LANDLORD")
    void updatingAnotherLandlordsListingReturns403() throws Exception {
        when(listingService.update(eq(10L), any())).thenThrow(new ForbiddenOperationException("Nu ai dreptul."));

        mockMvc.perform(put("/api/listings/10").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "LANDLORD")
    void getMissingListingReturns404() throws Exception {
        when(listingService.getOwned(99L)).thenThrow(new NotFoundException("Anunțul nu a fost găsit."));

        mockMvc.perform(get("/api/listings/mine/99"))
            .andExpect(status().isNotFound());
    }

    // ---- list ----

    @Test
    @WithMockUser(roles = "LANDLORD")
    void listMinePassesStatusAndCapsPageSize() throws Exception {
        when(listingService.listOwned(eq(ListingStatus.RENTED), any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 100, 0, 0));

        mockMvc.perform(get("/api/listings/mine").param("status", "RENTED").param("page", "2").param("size", "500"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(listingService).listOwned(eq(ListingStatus.RENTED), captor.capture());
        assertEquals(2, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    @WithMockUser(roles = "LANDLORD")
    void unknownStatusFilterReturns400() throws Exception {
        mockMvc.perform(get("/api/listings/mine").param("status", "SOLD"))
            .andExpect(status().isBadRequest());
    }

    // ---- status / delete ----

    @Test
    @WithMockUser(roles = "LANDLORD")
    void changeStatusReturnsUpdatedListing() throws Exception {
        when(listingService.changeStatus(10L, ListingStatus.RENTED)).thenReturn(ListingTestData.dto(10L, ListingStatus.RENTED));

        mockMvc.perform(patch("/api/listings/10/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "status": "RENTED" }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RENTED"));
    }

    @Test
    @WithMockUser(roles = "LANDLORD")
    void forbiddenStatusTransitionReturns409() throws Exception {
        // GEST-03
        when(listingService.changeStatus(10L, ListingStatus.UNAVAILABLE))
                .thenThrow(new ConflictException("Schimbarea statusului nu este permisă."));

        mockMvc.perform(patch("/api/listings/10/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "status": "UNAVAILABLE" }
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "LANDLORD")
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/listings/10"))
            .andExpect(status().isNoContent());

        verify(listingService).delete(10L);
    }

    @Test
    @WithMockUser(roles = "LANDLORD")
    void deletingAnotherLandlordsListingReturns403() throws Exception {
        doThrow(new ForbiddenOperationException("Nu ai dreptul.")).when(listingService).delete(10L);

        mockMvc.perform(delete("/api/listings/10"))
            .andExpect(status().isForbidden());
    }
}
