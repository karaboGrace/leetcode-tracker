package tracker;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class SM2SchedulerTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 1, 10);

    private LeetCodeCard newCard() {
        // adjust to your real constructor
        return new LeetCodeCard(1, "Two Sum", "https://leetcode.com/problems/two-sum/",
                Difficulty.EASY, java.util.List.of("array", "hash-map"));
    }

    @Test
    void firstPerfectReviewSchedulesTomorrow() {

        LeetCodeCard card = SM2Scheduler.review(newCard(), 5, TODAY);

        assertEquals(1, card.getRepetitions());
        assertEquals(1, card.getIntervalDays());
        assertEquals(TODAY.plusDays(1), card.getDueDate());
        assertEquals(2.6, card.getEasinessFactor(), 1e-9);
    }

    @Test
    void secondPassingReviewJumpsToSixDays() {
        LeetCodeCard card = SM2Scheduler.review(newCard(), 5, TODAY);
        card = SM2Scheduler.review(card, 5, TODAY.plusDays(1));

        assertEquals(6, card.getIntervalDays());
        assertEquals(TODAY.plusDays(7), card.getDueDate());
    }

    @Test
    void invalidQualityIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> SM2Scheduler.review(newCard(), 6, TODAY));
    }

    @Test
    void failedReviewResetsStreakAndSchedulesTomorrow() {
        LeetCodeCard card = SM2Scheduler.review(newCard(), 5, TODAY);               // reps 1, interval 1, EF 2.6
        card = SM2Scheduler.review(card, 5, TODAY.plusDays(1));                     // reps 2, interval 6, EF 2.7
        card = SM2Scheduler.review(card, 2, TODAY.plusDays(7));                     // fail

        assertEquals(0, card.getRepetitions());
        assertEquals(1, card.getIntervalDays());
        assertEquals(TODAY.plusDays(8), card.getDueDate());
        assertEquals(2.38, card.getEasinessFactor(), 1e-9);
    }

    @Test
    void easinessFactorNeverDropsBelowFloor() {
        LeetCodeCard card = newCard();

        for (int i = 0; i < 10; i++) {
            card = SM2Scheduler.review(card, 0, TODAY.plusDays(i));
            assertTrue(card.getEasinessFactor() >= 1.3);
        }

        assertEquals(1.3, card.getEasinessFactor(), 1e-9);
    }

    @Test
    void thirdPassingReviewMultipliesIntervalByEasinessFactor() {
        LeetCodeCard card = SM2Scheduler.review(newCard(), 5, TODAY);               // EF 2.6
        card = SM2Scheduler.review(card, 5, TODAY.plusDays(1));                     // EF 2.7, interval 6
        card = SM2Scheduler.review(card, 5, TODAY.plusDays(7));                     // interval = round(6 * 2.7)

        assertEquals(3, card.getRepetitions());
        assertEquals(16, card.getIntervalDays());
        assertEquals(TODAY.plusDays(23), card.getDueDate());
        assertEquals(2.8, card.getEasinessFactor(), 1e-9);
    }

    @Test
    void reviewRecordsTheDateItWasDone() {
        LeetCodeCard card = SM2Scheduler.review(newCard(), 4, TODAY);

        assertEquals(TODAY, card.getLastReviewed());
    }
}
