package tracker;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "cards")
public class LeetCodeCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String url;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    // EAGER for now so tags load with the card. We'll revisit this when we study the N+1 problem.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "card_tags", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    private double easinessFactor;
    private int repetitions;
    private int intervalDays;
    private LocalDate dueDate;
    private LocalDate lastReviewed;
    @Column(name = "user_id")
    private Long userId;

    // Required by JPA: Hibernate uses it to create objects when loading rows.
    protected LeetCodeCard() {
    }

    // For creating a brand-new card. The id stays null until the database assigns it on save.
    public LeetCodeCard(String title, String url, Difficulty difficulty, Set<String> tags) {
        this.title = title;
        this.url = url;
        this.difficulty = difficulty;
        this.tags = new HashSet<>(tags);
        this.easinessFactor = 2.5;
        this.repetitions = 0;
        this.intervalDays = 0;
        this.dueDate = LocalDate.now();
        this.lastReviewed = null;
    }

    public Long getId() {
        return id;
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

    public Set<String> getTags() {
        return Set.copyOf(tags);
    }

    public void setTags(Set<String> tags) {
        this.tags = new HashSet<>(tags);
    }

    public double getEasinessFactor() {
        return easinessFactor;
    }

    public void setEasinessFactor(double easinessFactor) {
        this.easinessFactor = easinessFactor;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isDue() {
        return !dueDate.isAfter(LocalDate.now());
    }

    // Two cards are the same card if they have the same database id.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeetCodeCard other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[%d] %-35s %-6s EF=%.2f reps=%d due=%s",
                id, title, difficulty, easinessFactor, repetitions, dueDate);
    }
}