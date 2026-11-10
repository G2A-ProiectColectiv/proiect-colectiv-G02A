package ro.rentalplatform.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ro.rentalplatform.auth.controller.AuthController;
import ro.rentalplatform.auth.dto.AuthResponse;
import ro.rentalplatform.auth.service.AuthService;
import ro.rentalplatform.common.exception.BadCredentialsAppException;
import ro.rentalplatform.common.exception.ConflictException;
import ro.rentalplatform.security.CorsConfig;
import ro.rentalplatform.security.JwtService;
import ro.rentalplatform.security.RestAccessDeniedHandler;
import ro.rentalplatform.security.RestAuthenticationEntryPoint;
import ro.rentalplatform.security.SecurityConfig;
import ro.rentalplatform.user.dto.UserDto;
import ro.rentalplatform.user.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class, CorsConfig.class})
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void registerUser() throws Exception {
        UserDto userDto = new UserDto(1L, "ana@test.ro", "Ana", "Pop",
            null, null, null, "TENANT", "ACTIVE");
        when(authService.register(any())).thenReturn(userDto);

        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "email": "ana@test.ro",
                    "password": "parola123",
                    "firstName": "Ana",
                    "lastName": "Pop",
                    "role": "TENANT"
                }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("ana@test.ro"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerWithInvalidData() throws Exception {
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "password": "123",
                    "firstName": "Ana",
                    "lastName": "Pop",
                    "role": "TENANT"
                }
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')]").exists())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'password')]").exists());

        verify(authService, never()).register(any());
    }

    @Test
    void registerWithDuplicateEmail() throws Exception {
        when(authService.register(any())).thenThrow(new ConflictException("Există deja un cont cu acest email."));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "email": "ana@test.ro",
                    "password": "parola123",
                    "firstName": "Ana",
                    "lastName": "Pop",
                    "role": "TENANT"
                }
                """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Există deja un cont cu acest email."));
    }

    @Test
    void successfulLoginWithToken() throws Exception {
        UserDto userDto = new UserDto(1L, "ana@test.ro", "Ana", "Pop",
            null, null, null, "TENANT", "ACTIVE");
        when(authService.login(any())).thenReturn(new AuthResponse("jwt-token", userDto));

        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "email": "ana@test.ro",
                    "password": "parola123"
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.user.email").value("ana@test.ro"));
    }

    @Test
    void badCredentialLogin() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsAppException("Email-ul sau parola sunt incorecte."));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "email": "ana@test.ro",
                    "password": "parola123"
                }
                """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }
}
