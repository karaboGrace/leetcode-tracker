# LeetCode Spaced Repetition Tracker

A command-line tool that schedules when to re-attempt LeetCode problems using
the SuperMemo-2 (SM-2) spaced-repetition algorithm, with data persisted to a
plain CSV file (no database, no external libraries just the JDK).

## Why

Solving a problem once doesn't mean you'll remember the pattern in three
weeks. This tracks every problem I've solved, grades how well I recalled
it on each re-attempt, and tells me exactly which problems are due today -
the same idea Anki uses for flashcards, applied to interview prep.

## Build & run

Requires JDK 17+ (built and tested on JDK 21).

```bash
javac -d out src/main/java/tracker/*.java

# Add a problem you just solved
java -cp out tracker.Tracker add "Two Sum" "https://leetcode.com/problems/two-sum" easy "array,hashmap"

# See what's due today
java -cp out tracker.Tracker due

# Review a problem (id 1) and grade your recall quality 0-5
java -cp out tracker.Tracker review 1 4

# See everything you're tracking
java -cp out tracker.Tracker list

# Summary stats
java -cp out tracker.Tracker stats
```

Data is stored in `data/cards.csv`, created automatically on first `add`.

## Quality scale

When you `review` a card, grade recall :

| Quality | Meaning |
|---|---|
| 5 | Solved instantly, optimal approach |
| 4 | Solved correctly, slower than you'd like |
| 3 | Solved, but needed a hint or had to think hard |
| 2 | Failed, but recognized the pattern once shown the answer |
| 1 | Failed, only vaguely recalled the idea |
| 0 | Complete blank |

Quality < 3 resets the repetition streak and schedules a re-attempt
tomorrow. Quality >= 3 grows the interval (1 day → 6 days → interval × EF),
and adjusts the easiness factor per the standard SM-2 formula.

## Architecture

- `SM2Scheduler` - pure algorithm logic, no I/O. The class to unit test.
- `LeetCodeCard` - data holder for a problem plus its current SM-2 state.
- `CardStore` - CSV persistence via `java.nio.file` + buffered I/O.
- `Difficulty` - enum with lenient parsing.
- `Tracker` - CLI entry point.

## Roadmap (planned, not yet built)

- Unit tests for `SM2Scheduler` (JUnit 5)
- Swap CSV for JSON or SQLite once the schema needs to grow (e.g. per-attempt history, not just latest state)
- Wrap `CardStore`/`SM2Scheduler` in a small FastAPI or Spring Boot REST layer
- React dashboard: due-today queue, streaks, EF trend chart per problem
- Tag-based analytics: which patterns (DP, sliding window, graphs) have the lowest average EF, i.e. where you're actually weak
