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

    private static final Long USER = 7L;

    @Mock
    private CardRepository repository;

    @InjectMocks
    private CardService service;

    private LeetCodeCard newCard() {
        return new LeetCodeCard("Two Sum", "https://leetcode.com/problems/two-sum/",
                Difficulty.EASY, Set.of("array"));
    }

    @Test
    void createSavesCardForTheUserWithDefaultSchedulingState() {
        when(repository.save(any(LeetCodeCard.class))).thenAnswer(inv -> inv.getArgument(0));
        CreateCardRequest request = new CreateCardRequest(
                "Two Sum", "https://leetcode.com/problems/two-sum/", Difficulty.EASY, Set.of("array"));

        LeetCodeCard saved = service.create(USER, request);

        assertEquals("Two Sum", saved.getTitle());
        assertEquals(USER, saved.getUserId());
        assertEquals(2.5, saved.getEasinessFactor(), 1e-9);
        assertEquals(0, saved.getRepetitions());
    }

    @Test
    void createWithNullTagsUsesEmptySet() {
        when(repository.save(any(LeetCodeCard.class))).thenAnswer(inv -> inv.getArgument(0));
        CreateCardRequest request = new CreateCardRequest(
                "Two Sum", "https://leetcode.com/problems/two-sum/", Difficulty.EASY, null);

        LeetCodeCard saved = service.create(USER, request);

        assertTrue(saved.getTags().isEmpty());
    }

    @Test
    void reviewAppliesTheScheduler() {
        when(repository.findByIdAndUserId(1L, USER)).thenReturn(Optional.of(newCard()));

        LeetCodeCard result = service.review(USER, 1L, 5);

        assertEquals(1, result.getRepetitions());
        assertEquals(1, result.getIntervalDays());
        assertEquals(2.6, result.getEasinessFactor(), 1e-9);
    }

    @Test
    void reviewOfUnknownCardThrowsNotFound() {
        when(repository.findByIdAndUserId(99L, USER)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> service.review(USER, 99L, 5));
    }

    @Test
    void anotherUsersCardIsTreatedAsNotFound() {
        when(repository.findByIdAndUserId(1L, 8L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> service.get(8L, 1L));
    }

    @Test
    void deleteOfUnknownCardThrowsAndDeletesNothing() {
        when(repository.existsByIdAndUserId(99L, USER)).thenReturn(false);

        assertThrows(CardNotFoundException.class, () -> service.delete(USER, 99L));
        verify(repository, never()).deleteById(any());
    }
}