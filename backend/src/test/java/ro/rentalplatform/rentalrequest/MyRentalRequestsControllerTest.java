package ro.rentalplatform.rentalrequest;

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
import ro.rentalplatform.common.exception.BadRequestException;
import ro.rentalplatform.common.exception.ConflictException;
import ro.rentalplatform.common.exception.ForbiddenOperationException;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.rentalrequest.controller.MyRentalRequestsController;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;
import ro.rentalplatform.rentalrequest.dto.ChangeRequestStatusDto;
import ro.rentalplatform.rentalrequest.service.RentalRequestService;
import ro.rentalplatform.security.CorsConfig;
import ro.rentalplatform.security.JwtService;
import ro.rentalplatform.security.RestAccessDeniedHandler;
import ro.rentalplatform.security.RestAuthenticationEntryPoint;
import ro.rentalplatform.security.SecurityConfig;
import ro.rentalplatform.user.repository.UserRepository;

import java.time.LocalDate;
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

@WebMvcTest(MyRentalRequestsController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class, CorsConfig.class})
class MyRentalRequestsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RentalRequestService requestService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;

    private static String body(String budgetMin, String budgetMax, LocalDate desiredFrom) {
        return """
            {
                "title": "Caut garsonieră",
                "description": "Aproape de facultate",
                "desiredType": "STUDIO",
                "budgetMin": %s,
                "budgetMax": %s,
                "currency": "EUR",
                "city": "București",
                "desiredFrom": "%s"
            }
            """.formatted(budgetMin, budgetMax, desiredFrom);
    }

    private static String validBody() {
        return body("250", "400", LocalDate.now().plusMonths(1));
    }

    // ---- roles (CER-01) ----

    @Test
    void withoutLoginReturns401() throws Exception {
        mockMvc.perform(get("/api/requests/mine"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LANDLORD")
    void landlordCannotCreateRequests() throws Exception {
        mockMvc.perform(post("/api/requests").contentType(MediaType.APPLICATION_JSON).content(validBody()))
            .andExpect(status().isForbidden());

        verify(requestService, never()).create(any());
    }

    // ---- create ----

    @Test
    @WithMockUser(roles = "TENANT")
    void tenantCreatesRequest() throws Exception {
        when(requestService.create(any())).thenReturn(RentalRequestTestData.dto(20L, RequestStatus.ACTIVE));

        mockMvc.perform(post("/api/requests").contentType(MediaType.APPLICATION_JSON).content(validBody()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(20))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "TENANT")
    void budgetMinGreaterThanMaxReturns400OnBudgetMax() throws Exception {
        // CER-04
        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("500", "400", LocalDate.now().plusMonths(1))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'budgetMax')]").exists());

        verify(requestService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "TENANT")
    void dateInThePastReturns400() throws Exception {
        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("250", "400", LocalDate.now().minusDays(1))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'desiredFrom')]").exists());
    }

    @Test
    @WithMockUser(roles = "TENANT")
    void incompleteZoneReturns400() throws Exception {
        when(requestService.create(any()))
                .thenThrow(new BadRequestException("radiusKm este obligatoriu când se furnizează un link Google Maps."));

        mockMvc.perform(post("/api/requests").contentType(MediaType.APPLICATION_JSON).content(validBody()))
            .andExpect(status().isBadRequest());
    }

    // ---- update / get ----

    @Test
    @WithMockUser(roles = "TENANT")
    void updatingAnotherTenantsRequestReturns403() throws Exception {
        when(requestService.update(eq(20L), any())).thenThrow(new ForbiddenOperationException("Nu ai dreptul."));

        mockMvc.perform(put("/api/requests/20").contentType(MediaType.APPLICATION_JSON).content(validBody()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TENANT")
    void getMissingRequestReturns404() throws Exception {
        when(requestService.getOwned(99L)).thenThrow(new NotFoundException("Cererea nu a fost găsită."));

        mockMvc.perform(get("/api/requests/mine/99"))
            .andExpect(status().isNotFound());
    }

    // ---- list ----

    @Test
    @WithMockUser(roles = "TENANT")
    void listMinePassesStatusAndCapsPageSize() throws Exception {
        when(requestService.listOwned(eq(RequestStatus.FOUND), any()))
                .thenReturn(new PageResponse<>(List.of(), 1, 100, 0, 0));

        mockMvc.perform(get("/api/requests/mine").param("status", "FOUND").param("page", "1").param("size", "1000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(requestService).listOwned(eq(RequestStatus.FOUND), captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
    }

    // ---- status / delete ----

    @Test
    @WithMockUser(roles = "TENANT")
    void changeStatusReturnsUpdatedRequest() throws Exception {
        when(requestService.changeStatus(20L, new ChangeRequestStatusDto(RequestStatus.FOUND)))
                .thenReturn(RentalRequestTestData.dto(20L, RequestStatus.FOUND));

        mockMvc.perform(patch("/api/requests/20/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "status": "FOUND" }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FOUND"));
    }

    @Test
    @WithMockUser(roles = "TENANT")
    void forbiddenStatusTransitionReturns409() throws Exception {
        // CER-06
        when(requestService.changeStatus(eq(20L), any())).thenThrow(new ConflictException("Nu este permisă."));

        mockMvc.perform(patch("/api/requests/20/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "status": "CLOSED" }
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "TENANT")
    void missingStatusReturns400() throws Exception {
        mockMvc.perform(patch("/api/requests/20/status").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'status')]").exists());
    }

    @Test
    @WithMockUser(roles = "TENANT")
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/requests/20"))
            .andExpect(status().isNoContent());

        verify(requestService).delete(20L);
    }

    @Test
    @WithMockUser(roles = "TENANT")
    void deletingAnotherTenantsRequestReturns403() throws Exception {
        doThrow(new ForbiddenOperationException("Nu ai dreptul.")).when(requestService).delete(20L);

        mockMvc.perform(delete("/api/requests/20"))
            .andExpect(status().isForbidden());
    }
}
