package tracker;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardStore {

    private final Path filePath;

    public CardStore(Path filePath) {
        this.filePath = filePath;
    }

    public void save(List<LeetCodeCard> cards) {
        List<String> lines = new ArrayList<>();
        lines.add("id,title,url,difficulty,tags,easinessFactor,repetitions,intervalDays,dueDate,lastReviewed");

        for (LeetCodeCard c : cards) {
            lines.add(toLine(c));
        }

        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.write(filePath, lines);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write " + filePath, e);
        }
    }
    public List<LeetCodeCard> load() {
        List<LeetCodeCard> cards = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return cards;
        }

        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) continue;
                cards.add(fromLine(line));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + filePath, e);
        }

        return cards;
    }

    private String toLine(LeetCodeCard c) {
        String tags = String.join(";", c.getTags());
        String lastReviewed = c.getLastReviewed() == null
                ? ""
                : c.getLastReviewed().format(DateTimeFormatter.ISO_LOCAL_DATE);

        return String.join(",",
                String.valueOf(c.getId()),
                c.getTitle(),
                c.getUrl(),
                c.getDifficulty().name(),
                tags,
                String.valueOf(c.getEasinessFactor()),
                String.valueOf(c.getRepetitions()),
                String.valueOf(c.getIntervalDays()),
                c.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                lastReviewed
        );
    }

    private LeetCodeCard fromLine(String line) {
        String[] parts = line.split(",", -1);

        int id = Integer.parseInt(parts[0]);
        String title = parts[1];
        String url = parts[2];
        Difficulty difficulty = Difficulty.fromString(parts[3]);

        List<String> tags = parts[4].isBlank()
                ? new ArrayList<>()
                : new ArrayList<>(Arrays.asList(parts[4].split(";")));

        double easinessFactor = Double.parseDouble(parts[5]);
        int repetitions = Integer.parseInt(parts[6]);
        int intervalDays = Integer.parseInt(parts[7]);
        LocalDate dueDate = LocalDate.parse(parts[8], DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate lastReviewed = parts[9].isBlank()
                ? null
                : LocalDate.parse(parts[9], DateTimeFormatter.ISO_LOCAL_DATE);

        return new LeetCodeCard(id, title, url, difficulty, tags,
                easinessFactor, repetitions, intervalDays, dueDate, lastReviewed);
    }
}