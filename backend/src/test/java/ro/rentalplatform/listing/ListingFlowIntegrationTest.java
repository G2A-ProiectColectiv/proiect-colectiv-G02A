package ro.rentalplatform.listing;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ro.rentalplatform.AbstractIntegrationTest;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.repository.ListingRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end checks from the "Verificare" sections of stage 1.3 (real security, database and parser). */
@AutoConfigureMockMvc
class ListingFlowIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "parola123";
    // raw coordinates are a valid "link" (LOC-10) and need no network call
    private static final String LOCATION = "44.4268, 26.1025";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListingRepository listingRepository;

    private String registerAndLogin(String email, String role) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "%s",
                        "password": "%s",
                        "firstName": "Ion",
                        "lastName": "Popescu",
                        "role": "%s"
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

    private static String listingBody(String title, String googleMapsUrl) {
        return """
            {
                "title": "%s",
                "description": "Luminos, aproape de metrou",
                "type": "APARTMENT",
                "price": 450,
                "currency": "EUR",
                "rooms": 2,
                "bathrooms": 1,
                "areaSqm": 55,
                "address": "Str. Exemplu 1",
                "city": "București",
                "amenities": ["BALCONY", "INTERNET"],
                "availableFrom": "2026-12-01",
                "googleMapsUrl": "%s"
            }
            """.formatted(title, googleMapsUrl);
    }

    private ResultActions createListing(String token, String googleMapsUrl) throws Exception {
        return mockMvc.perform(post("/api/listings")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(listingBody("Apartament 2 camere", googleMapsUrl)));
    }

    private Long createListingAndGetId(String token) throws Exception {
        String body = createListing(token, LOCATION)
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private ResultActions changeStatus(String token, Long id, String newStatus) throws Exception {
        return mockMvc.perform(patch("/api/listings/" + id + "/status")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                { "status": "%s" }
                """.formatted(newStatus)));
    }

    @Test
    void landlordCreatesListingWithCoordinatesFromLink() throws Exception {
        String token = registerAndLogin("ion@test.ro", "LANDLORD");

        createListing(token, LOCATION)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.latitude").value(44.4268))
            .andExpect(jsonPath("$.longitude").value(26.1025))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.amenities.length()").value(2));

        mockMvc.perform(get("/api/listings/mine").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void tenantCannotCreateListing() throws Exception {
        String token = registerAndLogin("ana@test.ro", "TENANT");

        createListing(token, LOCATION).andExpect(status().isForbidden());
    }

    @Test
    void linkWithoutCoordinatesReturns422() throws Exception {
        String token = registerAndLogin("ion@test.ro", "LANDLORD");

        createListing(token, "https://www.google.com/maps/place/Palatul+Parlamentului")
            .andExpect(status().isUnprocessableContent());
    }

    @Test
    void anotherLandlordCannotSeeOrEditTheListing() throws Exception {
        String ownerToken = registerAndLogin("ion@test.ro", "LANDLORD");
        String otherToken = registerAndLogin("maria@test.ro", "LANDLORD");
        Long id = createListingAndGetId(ownerToken);

        mockMvc.perform(put("/api/listings/" + id)
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(listingBody("Furat", LOCATION)))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/listings/mine/" + id).header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/listings/" + id).header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void statusChangesFollowTheAllowedTransitions() throws Exception {
        String token = registerAndLogin("ion@test.ro", "LANDLORD");
        Long id = createListingAndGetId(token);

        changeStatus(token, id, "RENTED").andExpect(status().isOk());
        mockMvc.perform(get("/api/listings/mine").param("status", "RENTED").header("Authorization", "Bearer " + token))
            .andExpect(jsonPath("$.totalElements").value(1));

        // GEST-03: RENTED -> UNAVAILABLE is not allowed directly
        changeStatus(token, id, "UNAVAILABLE").andExpect(status().isConflict());
        changeStatus(token, id, "ACTIVE").andExpect(status().isOk());
        changeStatus(token, id, "UNAVAILABLE").andExpect(status().isOk());
    }

    @Test
    void deletedListingDisappearsFromMyListings() throws Exception {
        String token = registerAndLogin("ion@test.ro", "LANDLORD");
        Long id = createListingAndGetId(token);

        mockMvc.perform(delete("/api/listings/" + id).header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/listings/mine/" + id).header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/listings/mine").header("Authorization", "Bearer " + token))
            .andExpect(jsonPath("$.totalElements").value(0));
        assertEquals(ListingStatus.DELETED, listingRepository.findById(id).orElseThrow().getStatus());
    }

    @Test
    void deletingTheAccountDeletesTheLandlordsListings() throws Exception {
        // 1.3.11: ListingUserDataCleaner is picked up by AccountServiceImpl automatically
        String token = registerAndLogin("ion@test.ro", "LANDLORD");
        Long first = createListingAndGetId(token);
        Long second = createListingAndGetId(token);

        mockMvc.perform(delete("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "password": "%s" }
                    """.formatted(PASSWORD)))
            .andExpect(status().isNoContent());

        assertEquals(ListingStatus.DELETED, listingRepository.findById(first).orElseThrow().getStatus());
        assertEquals(ListingStatus.DELETED, listingRepository.findById(second).orElseThrow().getStatus());
    }
}
