package ro.rentalplatform.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ro.rentalplatform.common.exception.ForbiddenOperationException;
import ro.rentalplatform.security.CorsConfig;
import ro.rentalplatform.security.JwtService;
import ro.rentalplatform.security.RestAccessDeniedHandler;
import ro.rentalplatform.security.RestAuthenticationEntryPoint;
import ro.rentalplatform.security.SecurityConfig;
import ro.rentalplatform.storage.InvalidImageException;
import ro.rentalplatform.user.controller.UserController;
import ro.rentalplatform.user.dto.UserDto;
import ro.rentalplatform.user.repository.UserRepository;
import ro.rentalplatform.user.service.AccountService;
import ro.rentalplatform.user.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
    RestAccessDeniedHandler.class, CorsConfig.class})
class UserControllerTest {

    private static final UserDto ANA = new UserDto(7L, "ana@test.ro", "Ana", "Pop",
            "0712345678", null, null, "TENANT", "ACTIVE");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AccountService accountService;
    // needed by JwtAuthFilter, which @WebMvcTest loads because it is a Filter
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;

    // ---- authentication ----

    @Test
    void getMeWithoutLoginReturns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));

        verify(userService, never()).getCurrentUser();
    }

    // ---- GET /me ----

    @Test
    @WithMockUser
    void getMeReturnsCurrentUser() throws Exception {
        when(userService.getCurrentUser()).thenReturn(ANA);

        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("ana@test.ro"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    // ---- PUT /me ----

    @Test
    @WithMockUser
    void updateProfileWithValidDataReturns200() throws Exception {
        when(userService.updateProfile(any())).thenReturn(ANA);

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "firstName": "Ana",
                        "lastName": "Pop",
                        "phone": "0712345678",
                        "bio": "Salut!"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Ana"));
    }

    @Test
    @WithMockUser
    void updateProfileWithInvalidPhoneReturns400() throws Exception {
        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "firstName": "Ana",
                        "lastName": "Pop",
                        "phone": "12345"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'phone')]").exists());

        verify(userService, never()).updateProfile(any());
    }

    @Test
    @WithMockUser
    void updateProfileWithTooLongBioReturns400() throws Exception {
        String bio = "a".repeat(1001);

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "firstName": "Ana",
                        "lastName": "Pop",
                        "bio": "%s"
                    }
                    """.formatted(bio)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'bio')]").exists());

        verify(userService, never()).updateProfile(any());
    }

    // ---- avatar ----

    @Test
    @WithMockUser
    void uploadAvatarReturns200WithUpdatedUser() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "me.png", "image/png", new byte[] {1, 2, 3});
        when(userService.updateAvatar(any())).thenReturn(ANA);

        mockMvc.perform(multipart("/api/users/me/avatar").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("ana@test.ro"));
    }

    @Test
    @WithMockUser
    void uploadInvalidAvatarReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[] {1, 2, 3});
        when(userService.updateAvatar(any())).thenThrow(new InvalidImageException("Doar fișiere JPG, PNG sau WEBP sunt acceptate."));

        mockMvc.perform(multipart("/api/users/me/avatar").file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser
    void removeAvatarReturns204() throws Exception {
        mockMvc.perform(delete("/api/users/me/avatar"))
            .andExpect(status().isNoContent());

        verify(userService).removeAvatar();
    }

    // ---- DELETE /me ----

    @Test
    @WithMockUser
    void deleteAccountWithPasswordReturns204() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "password": "parola123" }
                    """))
            .andExpect(status().isNoContent());

        verify(accountService).deleteOwnAccount("parola123");
    }

    @Test
    @WithMockUser
    void deleteAccountWithWrongPasswordReturns403() throws Exception {
        doThrow(new ForbiddenOperationException("Parola este incorectă."))
            .when(accountService).deleteOwnAccount("wrong");

        mockMvc.perform(delete("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "password": "wrong" }
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Parola este incorectă."));
    }

    @Test
    @WithMockUser
    void deleteAccountWithoutPasswordReturns400() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'password')]").exists());

        verify(accountService, never()).deleteOwnAccount(any());
    }
}
