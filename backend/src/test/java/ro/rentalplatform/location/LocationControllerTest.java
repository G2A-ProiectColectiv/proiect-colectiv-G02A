package ro.rentalplatform.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ro.rentalplatform.location.config.LocationProperties;
import ro.rentalplatform.location.controller.LocationController;
import ro.rentalplatform.location.domain.Coordinates;
import ro.rentalplatform.location.exception.CoordinatesNotFoundException;
import ro.rentalplatform.location.service.LocationService;
import ro.rentalplatform.security.CorsConfig;
import ro.rentalplatform.security.CurrentUserProvider;
import ro.rentalplatform.security.JwtService;
import ro.rentalplatform.security.RestAccessDeniedHandler;
import ro.rentalplatform.security.RestAuthenticationEntryPoint;
import ro.rentalplatform.security.SecurityConfig;
import ro.rentalplatform.user.repository.UserRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class, CorsConfig.class})
class LocationControllerTest {

    private static final String LINK = "https://maps.app.goo.gl/AbCdEf123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocationService locationService;
    @MockitoBean
    private CurrentUserProvider currentUserProvider;
    @MockitoBean
    private LocationProperties locationProperties;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(locationProperties.getRateLimitPerMinute()).thenReturn(2);
    }

    private ResultActions parse(String url) throws Exception {
        return mockMvc.perform(post("/api/location/parse")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                { "url": "%s" }
                """.formatted(url)));
    }

    @Test
    void withoutLoginReturns401() throws Exception {
        parse(LINK).andExpect(status().isUnauthorized());

        verify(locationService, never()).parse(anyString());
    }

    @Test
    @WithMockUser
    void returnsCoordinates() throws Exception {
        when(locationService.parse(LINK)).thenReturn(new Coordinates(44.4268, 26.1025));

        parse(LINK)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.latitude").value(44.4268))
            .andExpect(jsonPath("$.longitude").value(26.1025));
    }

    @Test
    @WithMockUser
    void linkWithoutCoordinatesReturns422WithMessage() throws Exception {
        when(locationService.parse(LINK)).thenThrow(new CoordinatesNotFoundException());

        parse(LINK)
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.message").value(CoordinatesNotFoundException.DEFAULT_MESSAGE));
    }

    @Test
    @WithMockUser
    void blankUrlReturns400() throws Exception {
        parse(" ")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'url')]").exists());
    }

    @Test
    @WithMockUser
    void tooManyRequestsReturns429() throws Exception {
        // LOC-12: the limit is stubbed to 2 per minute. The controller keeps one bucket per user and is
        // shared by all tests in this class, so this test uses its own user id to start with a full bucket.
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.of(429L));
        when(locationService.parse(LINK)).thenReturn(new Coordinates(44.4268, 26.1025));

        parse(LINK).andExpect(status().isOk());
        parse(LINK).andExpect(status().isOk());
        parse(LINK).andExpect(status().isTooManyRequests());
    }
}
