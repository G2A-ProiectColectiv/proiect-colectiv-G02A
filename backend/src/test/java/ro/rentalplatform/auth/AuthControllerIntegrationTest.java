package ro.rentalplatform.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void register_shouldCreateUserAndReturnToken() throws Exception {
        var request = "{\"email\":\"newuser@example.com\",\"password\":\"Password123!\",\"firstName\":\"Alice\",\"lastName\":\"Test\",\"role\":\"TENANT\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.user.role").value("TENANT"));
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() throws Exception {
        registerUser("loginuser@example.com", "Password123!");

        var request = "{\"email\":\"loginuser@example.com\",\"password\":\"Password123!\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.email").value("loginuser@example.com"));
    }

    @Test
    void login_shouldFailWhenPasswordIsWrong() throws Exception {
        registerUser("wrongpass@example.com", "Password123!");

        var request = "{\"email\":\"wrongpass@example.com\",\"password\":\"WrongPassword!\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_shouldFailWhenEmailAlreadyExists() throws Exception {
        registerUser("duplicate@example.com", "Password123!");

        var request = "{\"email\":\"duplicate@example.com\",\"password\":\"AnotherPass123!\",\"firstName\":\"Bob\",\"lastName\":\"Test\",\"role\":\"TENANT\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    private void registerUser(String email, String password) throws Exception {
        var request = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"firstName\":\"User\",\"lastName\":\"Test\",\"role\":\"TENANT\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());
    }
}
