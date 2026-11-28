package tracker;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    private static final String CREDENTIALS = """
            {"email":"grace@test.com","password":"correct-horse-9"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private MvcResult register(String body) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
    }

    private String tokenFrom(MvcResult result) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    @Test
    void registerReturnsTokenAndStoresOnlyAHash() throws Exception {
        MvcResult result = register(CREDENTIALS);

        assertEquals(201, result.getResponse().getStatus());
        assertFalse(tokenFrom(result).isBlank());

        AppUser stored = userRepository.findByEmail("grace@test.com").orElseThrow();
        assertNotEquals("correct-horse-9", stored.getPasswordHash());
        assertTrue(stored.getPasswordHash().startsWith("$2"));
    }

    @Test
    void duplicateEmailReturns409EvenWithDifferentCase() throws Exception {
        register(CREDENTIALS);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"Grace@Test.com\",\"password\":\"another-pass-1\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void loginTokenWorksOnAProtectedEndpoint() throws Exception {
        register(CREDENTIALS);

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(CREDENTIALS))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get("/api/cards").header("Authorization", "Bearer " + tokenFrom(login)))
                .andExpect(status().isOk());
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        register(CREDENTIALS);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"grace@test.com\",\"password\":\"not-the-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shortPasswordIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"grace@test.com\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void garbageTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/cards").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }
}