package tracker;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // each test rolls back, so tracker_test stays clean
class CardApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardRepository repository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private AppUser owner;
    private AppUser other;

    @BeforeEach
    void createUsers() {
        owner = userRepository.save(new AppUser("owner@test.com", "hash"));
        other = userRepository.save(new AppUser("other@test.com", "hash"));
    }

    private RequestPostProcessor loginAs(AppUser user) {
        return jwt().jwt(token -> token.subject(String.valueOf(user.getId())));
    }

    private LeetCodeCard saveCard(AppUser user, String title) {
        LeetCodeCard card = new LeetCodeCard(title, "https://leetcode.com/problems/" + title,
                Difficulty.MEDIUM, Set.of("array", "dp"));
        card.setUserId(user.getId());
        return repository.save(card);
    }

    @Test
    void createStoresCardForTheLoggedInUser() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .with(loginAs(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Two Sum","url":"https://leetcode.com/problems/two-sum/","difficulty":"EASY","tags":["array","hash-map"]}
                                """))
                .andExpect(status().isCreated());

        entityManager.flush();
        entityManager.clear();

        LeetCodeCard stored = repository.findByUserId(owner.getId()).get(0);
        assertEquals("Two Sum", stored.getTitle());
        assertEquals(Set.of("array", "hash-map"), stored.getTags());
        assertEquals(owner.getId(), stored.getUserId());
    }

    @Test
    void reviewPersistsTheNewSchedule() throws Exception {
        LeetCodeCard card = saveCard(owner, "coin-change");

        mockMvc.perform(post("/api/cards/" + card.getId() + "/review")
                        .with(loginAs(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quality\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repetitions").value(1));

        entityManager.flush();
        entityManager.clear();

        LeetCodeCard reloaded = repository.findById(card.getId()).orElseThrow();
        assertEquals(1, reloaded.getRepetitions());
        assertEquals(1, reloaded.getIntervalDays());
        assertEquals(2.6, reloaded.getEasinessFactor(), 1e-9);
        assertEquals(LocalDate.now(), reloaded.getLastReviewed());
    }

    @Test
    void dueOnlyReturnsOnlyTheUsersOwnCardsDueToday() throws Exception {
        saveCard(owner, "due-today");
        LeetCodeCard later = saveCard(owner, "due-later");
        later.setDueDate(LocalDate.now().plusDays(5));
        repository.save(later);
        saveCard(other, "someone-elses");

        mockMvc.perform(get("/api/cards").param("dueOnly", "true").with(loginAs(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("due-today"));
    }

    @Test
    void deleteRemovesTheCard() throws Exception {
        LeetCodeCard card = saveCard(owner, "to-delete");
        Long id = card.getId();

        mockMvc.perform(delete("/api/cards/" + id).with(loginAs(owner)))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        assertFalse(repository.existsById(id));
    }

    @Test
    void unknownCardReturns404ThroughTheWholeStack() throws Exception {
        mockMvc.perform(get("/api/cards/999999").with(loginAs(owner)))
                .andExpect(status().isNotFound());
    }

    @Test
    void anotherUsersCardLooksLikeItDoesNotExist() throws Exception {
        LeetCodeCard card = saveCard(owner, "private-card");
        String path = "/api/cards/" + card.getId();

        mockMvc.perform(get(path).with(loginAs(other))).andExpect(status().isNotFound());
        mockMvc.perform(post(path + "/review").with(loginAs(other))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quality\":5}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete(path).with(loginAs(other))).andExpect(status().isNotFound());

        assertTrue(repository.existsById(card.getId()));
    }

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/cards")).andExpect(status().isUnauthorized());
    }
}