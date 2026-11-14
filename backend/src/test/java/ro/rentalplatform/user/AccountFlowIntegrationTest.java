package ro.rentalplatform.user;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ro.rentalplatform.AbstractIntegrationTest;
import ro.rentalplatform.notification.EmailService;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;
import ro.rentalplatform.user.repository.UserRepository;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end checks from the "Verificare" sections of stage 1.2 (password reset, account deletion). */
@AutoConfigureMockMvc
class AccountFlowIntegrationTest extends AbstractIntegrationTest {

    private static final String EMAIL = "ana@test.ro";
    private static final String PASSWORD = "parola123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    // replaced by a mock so the test can read the reset link that would have been emailed
    @MockitoBean
    private EmailService emailService;

    private ResultActions register() throws Exception {
        return mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "email": "%s",
                    "password": "%s",
                    "firstName": "Ana",
                    "lastName": "Pop",
                    "role": "TENANT"
                }
                """.formatted(EMAIL, PASSWORD)));
    }

    private ResultActions login(String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                { "email": "%s", "password": "%s" }
                """.formatted(EMAIL, password)));
    }

    private String loginAndGetToken() throws Exception {
        String body = login(PASSWORD)
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private ResultActions requestReset(String email) throws Exception {
        return mockMvc.perform(post("/api/auth/password-reset/request")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                { "email": "%s" }
                """.formatted(email)));
    }

    private ResultActions confirmReset(String token, String newPassword) throws Exception {
        return mockMvc.perform(post("/api/auth/password-reset/confirm")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                { "token": "%s", "newPassword": "%s" }
                """.formatted(token, newPassword)));
    }

    @SuppressWarnings("unchecked")
    private String tokenFromResetEmail() {
        ArgumentCaptor<Map<String, Object>> variables = ArgumentCaptor.forClass(Map.class);
        verify(emailService).send(eq(EMAIL), anyString(), anyString(), variables.capture());
        String link = (String) variables.getValue().get("resetLink");
        return link.substring(link.indexOf("token=") + "token=".length());
    }

    // ---- password reset (1.2.7) ----

    @Test
    void resetRequestForUnknownEmailStillReturns204() throws Exception {
        // the response must not reveal whether the email exists
        requestReset("nobody@test.ro").andExpect(status().isNoContent());
    }

    @Test
    void passwordResetChangesPasswordAndTokenWorksOnlyOnce() throws Exception {
        register().andExpect(status().isCreated());
        requestReset(EMAIL).andExpect(status().isNoContent());
        String token = tokenFromResetEmail();

        confirmReset(token, "parolaNoua123").andExpect(status().isNoContent());

        login("parolaNoua123").andExpect(status().isOk());
        login(PASSWORD).andExpect(status().isUnauthorized());
        // same token a second time → 400
        confirmReset(token, "altaParola123").andExpect(status().isBadRequest());
    }

    @Test
    void resetConfirmWithMadeUpTokenReturns400() throws Exception {
        confirmReset("not-a-real-token", "parolaNoua123")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    // ---- account deletion (1.2.8) ----

    @Test
    void deletedAccountIsAnonymisedAndCanNoLongerBeUsed() throws Exception {
        register().andExpect(status().isCreated());
        String token = loginAndGetToken();
        Long userId = userRepository.findByEmail(EMAIL).orElseThrow().getId();

        mockMvc.perform(delete("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "password": "%s" }
                    """.formatted(PASSWORD)))
            .andExpect(status().isNoContent());

        User deleted = userRepository.findById(userId).orElseThrow();
        assertEquals(UserStatus.DELETED, deleted.getStatus());
        assertEquals("deleted_" + userId + "@removed.local", deleted.getEmail());
        assertNull(deleted.getPhone());

        // the old token is rejected, and the old email can no longer log in
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
        login(PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    void deleteWithWrongPasswordReturns403AndKeepsAccount() throws Exception {
        register().andExpect(status().isCreated());
        String token = loginAndGetToken();

        mockMvc.perform(delete("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "password": "wrong-password" }
                    """))
            .andExpect(status().isForbidden());

        login(PASSWORD).andExpect(status().isOk());
    }
}
