package tracker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository repository;

    @InjectMocks
    private CardService service;

    private LeetCodeCard newCard() {
        return new LeetCodeCard("Two Sum", "https://leetcode.com/problems/two-sum/",
                Difficulty.EASY, Set.of("array"));
    }

    @Test
    void createSavesCardWithDefaultSchedulingState() {
        when(repository.save(any(LeetCodeCard.class))).thenAnswer(inv -> inv.getArgument(0));
        CreateCardRequest request = new CreateCardRequest(
                "Two Sum", "https://leetcode.com/problems/two-sum/", Difficulty.EASY, Set.of("array"));

        LeetCodeCard saved = service.create(request);

        assertEquals("Two Sum", saved.getTitle());
        assertEquals(2.5, saved.getEasinessFactor(), 1e-9);
        assertEquals(0, saved.getRepetitions());
        verify(repository).save(any(LeetCodeCard.class));
    }

    @Test
    void createWithNullTagsUsesEmptySet() {
        when(repository.save(any(LeetCodeCard.class))).thenAnswer(inv -> inv.getArgument(0));
        CreateCardRequest request = new CreateCardRequest(
                "Two Sum", "https://leetcode.com/problems/two-sum/", Difficulty.EASY, null);

        LeetCodeCard saved = service.create(request);

        assertTrue(saved.getTags().isEmpty());
    }

    @Test
    void reviewAppliesTheScheduler() {
        LeetCodeCard card = newCard();
        when(repository.findById(1L)).thenReturn(Optional.of(card));

        LeetCodeCard result = service.review(1L, 5);

        assertEquals(1, result.getRepetitions());
        assertEquals(1, result.getIntervalDays());
        assertEquals(2.6, result.getEasinessFactor(), 1e-9);
    }

    @Test
    void reviewOfUnknownCardThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> service.review(99L, 5));
    }

    @Test
    void deleteOfUnknownCardThrowsAndDeletesNothing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(CardNotFoundException.class, () -> service.delete(99L));
        verify(repository, never()).deleteById(any());
    }
}