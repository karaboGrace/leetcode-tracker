package tracker;

import java.time.LocalDate;
import java.util.Set;

public record CardResponse(
        Long id,
        String title,
        String url,
        Difficulty difficulty,
        Set<String> tags,
        double easinessFactor,
        int repetitions,
        int intervalDays,
        LocalDate dueDate,
        LocalDate lastReviewed) {

    public static CardResponse from(LeetCodeCard card) {
        return new CardResponse(
                card.getId(),
                card.getTitle(),
                card.getUrl(),
                card.getDifficulty(),
                card.getTags(),
                card.getEasinessFactor(),
                card.getRepetitions(),
                card.getIntervalDays(),
                card.getDueDate(),
                card.getLastReviewed());
    }
}