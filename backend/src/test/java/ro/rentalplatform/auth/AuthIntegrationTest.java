package ro.rentalplatform.auth;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ro.rentalplatform.AbstractIntegrationTest;
import ro.rentalplatform.user.domain.Role;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class AuthIntegrationTest extends AbstractIntegrationTest {
    private static final String PASSWORD = "parola123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private ResultActions register(String email) throws Exception {
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
                          """.formatted(email, PASSWORD)));
    }

    private ResultActions login(String email) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                          {
                            "email": "%s",
                            "password": "%s"
                          }
                          """.formatted(email, PASSWORD)));
    }

    private String loginAndGetToken(String email) throws Exception {
        String body = login(email)
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    @Test
    void registeredUserCanLogIn() throws Exception {
        register("ana@test.ro").andExpect(status().isCreated());

        login("ana@test.ro")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.user.email").value("ana@test.ro"));
    }

    @Test
    void duplicateMailInDifferentCase() throws Exception {
        register("ana@test.ro").andExpect(status().isCreated());

        register("ANA@test.ro").andExpect(status().isConflict());
    }

    @Test
    void passwordStoredHashed() throws Exception {
        register("ana@test.ro").andExpect(status().isCreated());

        User user = userRepository.findByEmail("ana@test.ro").orElseThrow();

        assertNotEquals(PASSWORD, user.getPasswordHash());
        assertTrue(user.getPasswordHash().startsWith("$2"));
    }

    @Test
    void unauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/alabala"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void authorizedWithToken() throws Exception {
        register("ana@test.ro").andExpect(status().isCreated());
        String token = loginAndGetToken("ana@test.ro");

        mockMvc.perform(get("/api/alabala").header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    @Test
    void adminInsertedAutomatically() {
        Optional<User> opt = userRepository.findByEmail("admin@rental.local");

        assertTrue(opt.isPresent());
        assertEquals(Role.ADMIN, opt.get().getRole());
    }
}
