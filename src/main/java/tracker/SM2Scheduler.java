package tracker;

import java.time.LocalDate;

public final class SM2Scheduler {

    private static final double MIN_EASINESS_FACTOR = 1.3;
    private static final int PASSING_QUALITY = 3;

    private SM2Scheduler() {} // static-only utility class

    // Version 1: used by your CLI and GUI.
    public static LeetCodeCard review(LeetCodeCard card, int quality) {
        return review(card, quality, LocalDate.now());
    }

    // Version 2: takes the date as a parameter. Tests use this one.
    public static LeetCodeCard review(LeetCodeCard card, int quality, LocalDate today) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("quality must be between 0 and 5, got " + quality);
        }

        double ef = card.getEasinessFactor();
        int repetitions = card.getRepetitions();
        int interval;

        if (quality < PASSING_QUALITY) {
            // Failed recall: reset the repetition streak, review again tomorrow.
            repetitions = 0;
            interval = 1;
        } else {
            if (repetitions == 0) {
                interval = 1;
            } else if (repetitions == 1) {
                interval = 6;
            } else {
                interval = (int) Math.round(card.getIntervalDays() * ef);
            }
            repetitions += 1;
        }

        // EF update formula, from the original SuperMemo-2 paper.
        double newEf = ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        if (newEf < MIN_EASINESS_FACTOR) {
            newEf = MIN_EASINESS_FACTOR;
        }

        card.setEasinessFactor(newEf);
        card.setRepetitions(repetitions);
        card.setIntervalDays(interval);
        card.setLastReviewed(today);
        card.setDueDate(today.plusDays(interval));

        return card;
    }
}