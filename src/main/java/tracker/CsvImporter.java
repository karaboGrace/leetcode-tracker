package tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CsvImporter implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CsvImporter.class);

    private final CardRepository repository;
    private final String csvPath;

    // The path comes from --app.import-csv=... ; when it's blank the importer does nothing.
    public CsvImporter(CardRepository repository, @Value("${app.import-csv:}") String csvPath) {
        this.repository = repository;
        this.csvPath = csvPath;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (csvPath.isBlank()) {
            return;
        }
        importFile(Path.of(csvPath));
    }

    void importFile(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty()) {
            log.warn("CSV import: file is empty");
            return;
        }

        String[] header = lines.get(0).split(",", -1);
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            columns.put(normalize(header[i]), i);
        }

        int title = require(columns, lines.get(0), "title");
        int url = require(columns, lines.get(0), "url", "link");
        int difficulty = require(columns, lines.get(0), "difficulty");
        int tags = find(columns, "tags");
        int ef = find(columns, "easinessfactor", "ef", "easiness");
        int repetitions = find(columns, "repetitions", "reps");
        int interval = find(columns, "intervaldays", "interval");
        int due = find(columns, "duedate", "due");
        int lastReviewed = find(columns, "lastreviewed", "lastreview");

        int imported = 0;
        int duplicates = 0;
        int skipped = 0;

        for (int n = 1; n < lines.size(); n++) {
            String line = lines.get(n);
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(",", -1);
            if (f.length != header.length) {
                skipped++;
                log.warn("CSV import: line {} skipped (expected {} fields, found {}): {}",
                        n + 1, header.length, f.length, line);
                continue;
            }

            String cardUrl = value(f, url);
            if (repository.existsByUrl(cardUrl)) {
                duplicates++;
                continue;
            }

            LeetCodeCard card = new LeetCodeCard(
                    value(f, title), cardUrl, parseDifficulty(value(f, difficulty)), parseTags(value(f, tags)));

            String efText = value(f, ef);
            if (!efText.isEmpty()) card.setEasinessFactor(Double.parseDouble(efText));
            String repsText = value(f, repetitions);
            if (!repsText.isEmpty()) card.setRepetitions(Integer.parseInt(repsText));
            String intervalText = value(f, interval);
            if (!intervalText.isEmpty()) card.setIntervalDays(Integer.parseInt(intervalText));
            LocalDate dueDate = parseDate(value(f, due));
            if (dueDate != null) card.setDueDate(dueDate);
            card.setLastReviewed(parseDate(value(f, lastReviewed)));

            repository.save(card);
            imported++;
        }

        log.info("CSV import finished: {} imported, {} already present, {} skipped", imported, duplicates, skipped);
    }

    private static String normalize(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private static int find(Map<String, Integer> columns, String... names) {
        for (String name : names) {
            Integer index = columns.get(name);
            if (index != null) return index;
        }
        return -1;
    }

    private static int require(Map<String, Integer> columns, String headerLine, String... names) {
        int index = find(columns, names);
        if (index < 0) {
            throw new IllegalStateException(
                    "CSV import: no column named " + String.join(" or ", names) + ". Header was: " + headerLine);
        }
        return index;
    }

    private static String value(String[] fields, int index) {
        return index < 0 ? "" : fields[index].trim();
    }

    private static Difficulty parseDifficulty(String text) {
        try {
            return Difficulty.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Difficulty.MEDIUM;
        }
    }

    private static Set<String> parseTags(String text) {
        Set<String> tags = new HashSet<>();
        for (String tag : text.split(";")) {
            if (!tag.isBlank()) tags.add(tag.trim());
        }
        return tags;
    }

    private static LocalDate parseDate(String text) {
        if (text.isEmpty() || text.equalsIgnoreCase("null")) return null;
        return LocalDate.parse(text);
    }
}