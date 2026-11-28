package tracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@Import(SecurityConfig.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService service;

    // The security setup needs a JwtDecoder bean; the tests bypass real tokens with jwt() below.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor asUser7() {
        return jwt().jwt(token -> token.subject("7"));
    }

    private LeetCodeCard cardWithId(long id) {
        LeetCodeCard card = new LeetCodeCard("Two Sum", "https://leetcode.com/problems/two-sum/",
                Difficulty.EASY, Set.of("array"));
        ReflectionTestUtils.setField(card, "id", id);
        return card;
    }

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        when(service.create(eq(7L), any(CreateCardRequest.class))).thenReturn(cardWithId(1L));

        mockMvc.perform(post("/api/cards")
                        .with(asUser7())
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
                        .with(asUser7())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","url":"https://leetcode.com/problems/two-sum/","difficulty":"EASY"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reviewWithQualityAboveFiveReturns400() throws Exception {
        mockMvc.perform(post("/api/cards/1/review")
                        .with(asUser7())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quality\":9}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUnknownCardReturns404() throws Exception {
        when(service.get(7L, 99L)).thenThrow(new CardNotFoundException(99L));

        mockMvc.perform(get("/api/cards/99").with(asUser7()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Card not found"));
    }

    @Test
    void listReturnsTheUsersCards() throws Exception {
        when(service.list(7L, false)).thenReturn(List.of(cardWithId(1L)));

        mockMvc.perform(get("/api/cards").with(asUser7()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Two Sum"));
    }

    @Test
    void requestWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isUnauthorized());
    }
}