package tracker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Tracker {

    private static final Path DATA_FILE = Path.of("data", "cards.csv");

    public static void main(String[] args) {
        CardStore store = new CardStore(DATA_FILE);
        List<LeetCodeCard> cards = new ArrayList<>(store.load());

        if (args.length == 0) {
            System.out.println("Usage: add | review | due | list");
            return;
        }

        String command = args[0];
        switch (command) {
            case "add":
                handleAdd(cards, args, store);
                break;
            case "review":
                handleReview(cards, args, store);
                break;
            case "due":
                handleDue(cards);
                break;
            case "list":
                handleList(cards);
                break;
            default:
                System.out.println("Unknown command: " + command);
        }
    }

    private static void handleAdd(List<LeetCodeCard> cards, String[] args, CardStore store) {
        String title = args[1];
        String url = args[2];
        Difficulty difficulty = Difficulty.fromString(args[3]);

        int nextId = cards.size() + 1;

        LeetCodeCard card = new LeetCodeCard(nextId, title, url, difficulty, new ArrayList<>());
        cards.add(card);
        store.save(cards);

        System.out.println("Added: " + card);
    }

    private static void handleReview(List<LeetCodeCard> cards, String[] args, CardStore store) {
        int id = Integer.parseInt(args[1]);
        int quality = Integer.parseInt(args[2]);

        Optional<LeetCodeCard> match = cards.stream()
                .filter(c -> c.getId() == id)
                .findFirst();

        if (match.isEmpty()) {
            System.out.println("No card with id " + id);
            return;
        }

        LeetCodeCard card = match.get();
        SM2Scheduler.review(card, quality);
        store.save(cards);

        System.out.println("Reviewed: " + card);
    }

    private static void handleDue(List<LeetCodeCard> cards) {
        for (LeetCodeCard c : cards) {
            if (c.isDue()) {
                System.out.println(c);
            }
        }
    }

    private static void handleList(List<LeetCodeCard> cards) {
        for (LeetCodeCard c : cards) {
            System.out.println(c);
        }
    }
}