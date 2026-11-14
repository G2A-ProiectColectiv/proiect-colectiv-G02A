package ro.rentalplatform.listing;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.rentalplatform.AbstractIntegrationTest;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The "Verificare" steps of 1.5.3 over real HTTP: public search and details, with and without a token. */
@AutoConfigureMockMvc
class PublicSearchIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "parola123";

    @Autowired
    private MockMvc mockMvc;

    private String landlordToken;
    private Long listingId;

    @BeforeEach
    void createLandlordWithListing() throws Exception {
        landlordToken = registerAndLogin("ion@test.ro", "LANDLORD");

        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + landlordToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "firstName": "Ion", "lastName": "Popescu", "phone": "0711111111" }
                    """))
            .andExpect(status().isOk());

        String body = mockMvc.perform(post("/api/listings")
                .header("Authorization", "Bearer " + landlordToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Apartament Floreasca",
                        "description": "Luminos",
                        "type": "APARTMENT",
                        "price": 600,
                        "currency": "EUR",
                        "rooms": 3,
                        "bathrooms": 2,
                        "areaSqm": 80,
                        "address": "Str. Exemplu 1",
                        "city": "București",
                        "availableFrom": "2026-12-01",
                        "googleMapsUrl": "44.4268, 26.1025"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        listingId = ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private String registerAndLogin(String email, String role) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "%s", "password": "%s",
                        "firstName": "Ion", "lastName": "Popescu", "role": "%s"
                    }
                    """.formatted(email, PASSWORD, role)))
            .andExpect(status().isCreated());
        String body = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "%s", "password": "%s" }
                    """.formatted(email, PASSWORD)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    @Test
    void searchAndMapWorkWithoutToken() throws Exception {
        mockMvc.perform(get("/api/listings").param("city", "bucuresti"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.items[0].id").value(listingId));

        mockMvc.perform(get("/api/listings/map").param("bbox", "44.33,25.96,44.55,26.25"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(listingId));
    }

    @Test
    void detailsWithoutTokenHaveNoPhone() throws Exception {
        mockMvc.perform(get("/api/listings/" + listingId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.owner.firstName").value("Ion"))
            .andExpect(jsonPath("$.owner.phone").value(nullValue()));
    }

    @Test
    void detailsWithTokenIncludePhone() throws Exception {
        String tenantToken = registerAndLogin("ana@test.ro", "TENANT");

        mockMvc.perform(get("/api/listings/" + listingId).header("Authorization", "Bearer " + tenantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.owner.phone").value("0711111111"));
    }

    @Test
    void invalidTokenOnPublicRouteIsTreatedAsAnonymous() throws Exception {
        mockMvc.perform(get("/api/listings/" + listingId).header("Authorization", "Bearer not-a-real-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.owner.phone").value(nullValue()));
    }

    @Test
    void myListingsWithoutTokenStillReturn401() throws Exception {
        mockMvc.perform(get("/api/listings/mine")).andExpect(status().isUnauthorized());
    }
}
