
package com.dhanush.ticketing.security;

import com.dhanush.ticketing.SharedTestContainers;
import com.dhanush.ticketing.user.User;
import com.dhanush.ticketing.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAuthorizationIntegrationTest extends SharedTestContainers {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void protectedEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicEventListingAllowsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk());
    }

    @Test
    void eventCreationRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserCannotCreateEvent() throws Exception {
        User user = userRepository.findByEmail("user@example.com")
                .orElseThrow(() ->
                        new IllegalStateException("Seed USER account not found"));

        String token = jwtService.generateToken(user);

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizerCanPassEventCreationAuthorization() throws Exception {
        User organizer = userRepository.findByEmail("organizer@example.com")
                .orElseThrow(() ->
                        new IllegalStateException("Seed ORGANIZER account not found"));

        String token = jwtService.generateToken(organizer);

        // The request may fail validation, but authorization should not reject the organizer.
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(result ->
                        assertNotEquals(401, result.getResponse().getStatus()))
                .andExpect(result ->
                        assertNotEquals(403, result.getResponse().getStatus()));
    }
}
