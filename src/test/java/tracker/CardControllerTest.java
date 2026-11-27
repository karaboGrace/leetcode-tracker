package tracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService service;

    private LeetCodeCard cardWithId(long id) {
        LeetCodeCard card = new LeetCodeCard("Two Sum", "https://leetcode.com/problems/two-sum/",
                Difficulty.EASY, Set.of("array"));
        ReflectionTestUtils.setField(card, "id", id);
        return card;
    }

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        when(service.create(any(CreateCardRequest.class))).thenReturn(cardWithId(1L));

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Two Sum","url":"https://leetcode.com/problems/two-sum/","difficulty":"EASY","tags":["array"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/cards/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Two Sum"));
    }

    @Test
    void createWithBlankTitleReturns400() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","url":"https://leetcode.com/problems/two-sum/","difficulty":"EASY"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reviewWithQualityAboveFiveReturns400() throws Exception {
        mockMvc.perform(post("/api/cards/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quality\":9}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUnknownCardReturns404() throws Exception {
        when(service.get(99L)).thenThrow(new CardNotFoundException(99L));

        mockMvc.perform(get("/api/cards/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Card not found"));
    }

    @Test
    void listReturnsCards() throws Exception {
        when(service.list(false)).thenReturn(List.of(cardWithId(1L)));

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Two Sum"));
    }
}