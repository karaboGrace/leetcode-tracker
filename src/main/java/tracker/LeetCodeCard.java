package tracker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LeetCodeCard {

    private final int id;
    private String title;
    private String url;
    private Difficulty difficulty;
    private List<String> tags;

    private double easinessFactor;
    private int repetitions;
    private int intervalDays;
    private LocalDate dueDate;
    private LocalDate lastReviewed;

    public LeetCodeCard(int id, String title, String url, Difficulty difficulty, List<String> tags) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.difficulty = difficulty;
        this.tags = new ArrayList<>(tags);
        this.easinessFactor = 2.5;
        this.repetitions = 0;
        this.intervalDays = 0;
        this.dueDate = LocalDate.now();
        this.lastReviewed = null;
    }

    public int getId() {
        return id;
    }

    public double getEasinessFactor() {
        return easinessFactor; }

    public void setEasinessFactor(double easinessFactor) {
        this.easinessFactor = easinessFactor;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public List<String> getTags() {
        return List.copyOf(tags);
    }

    public void setTags(List<String> tags) {
        this.tags = new ArrayList<>(tags);
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(int intervalDays) {
        this.intervalDays = intervalDays;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(LocalDate lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    public boolean isDue() {
        return !dueDate.isAfter(LocalDate.now());
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[%d] %-35s %-6s EF=%.2f reps=%d due=%s",
                id, title, difficulty, easinessFactor, repetitions, dueDate);
    }

    public LeetCodeCard(int id, String title, String url, Difficulty difficulty, List<String> tags,
                        double easinessFactor, int repetitions, int intervalDays,
                        LocalDate dueDate, LocalDate lastReviewed) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.difficulty = difficulty;
        this.tags = new ArrayList<>(tags);
        this.easinessFactor = easinessFactor;
        this.repetitions = repetitions;
        this.intervalDays = intervalDays;
        this.dueDate = dueDate;
        this.lastReviewed = lastReviewed;
    }
}
