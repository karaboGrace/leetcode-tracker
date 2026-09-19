package tracker;

public enum Difficulty {
    EASY, MEDIUM, HARD;
    public static Difficulty fromString(String s) {
        try {
            return Difficulty.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: unknown difficulty '" + s + "', defaulting to MEDIUM");
            return MEDIUM;
        }

    }

}
