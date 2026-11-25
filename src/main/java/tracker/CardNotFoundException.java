package tracker;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(Long id) {
        super("No card with id " + id);
    }
}