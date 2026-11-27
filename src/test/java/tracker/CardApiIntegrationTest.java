package tracker;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
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

    @PersistenceContext
    private EntityManager entityManager;

    private LeetCodeCard saveCard(String title) {
        return repository.save(new LeetCodeCard(title, "https://leetcode.com/problems/" + title,
                Difficulty.MEDIUM, Set.of("array", "dp")));
    }

    @Test
    void createStoresCardAndTagsInPostgres() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Two Sum","url":"https://leetcode.com/problems/two-sum/","difficulty":"EASY","tags":["array","hash-map"]}
                                """))
                .andExpect(status().isCreated());

        entityManager.flush();
        entityManager.clear();

        LeetCodeCard stored = repository.findAll().get(0);
        assertEquals("Two Sum", stored.getTitle());
        assertEquals(Set.of("array", "hash-map"), stored.getTags());
        assertNotNull(stored.getId());
    }

    @Test
    void reviewPersistsTheNewSchedule() throws Exception {
        LeetCodeCard card = saveCard("coin-change");

        mockMvc.perform(post("/api/cards/" + card.getId() + "/review")
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
    void dueOnlyReturnsOnlyCardsDueToday() throws Exception {
        saveCard("due-today");
        LeetCodeCard later = saveCard("due-later");
        later.setDueDate(LocalDate.now().plusDays(5));
        repository.save(later);

        mockMvc.perform(get("/api/cards").param("dueOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("due-today"));
    }

    @Test
    void deleteRemovesTheCard() throws Exception {
        LeetCodeCard card = saveCard("to-delete");
        Long id = card.getId();

        mockMvc.perform(delete("/api/cards/" + id))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        assertFalse(repository.existsById(id));
    }

    @Test
    void unknownCardReturns404ThroughTheWholeStack() throws Exception {
        mockMvc.perform(get("/api/cards/999999"))
                .andExpect(status().isNotFound());
    }
}