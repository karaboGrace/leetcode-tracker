package tracker;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class CardService {

    private final CardRepository repository;

    public CardService(CardRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LeetCodeCard create(Long userId, CreateCardRequest request) {
        Set<String> tags = request.tags() == null ? Set.of() : request.tags();
        LeetCodeCard card = new LeetCodeCard(
                request.title(), request.url(), request.difficulty(), tags);
        card.setUserId(userId);
        return repository.save(card);
    }

    @Transactional(readOnly = true)
    public List<LeetCodeCard> list(Long userId, boolean dueOnly) {
        if (dueOnly) {
            return repository.findByUserIdAndDueDateLessThanEqualOrderByDueDateAsc(userId, LocalDate.now());
        }
        return repository.findByUserId(userId);
    }

    // Another user's card is reported as "not found", so its existence isn't revealed.
    @Transactional(readOnly = true)
    public LeetCodeCard get(Long userId, Long id) {
        return repository.findByIdAndUserId(id, userId).orElseThrow(() -> new CardNotFoundException(id));
    }

    @Transactional
    public LeetCodeCard review(Long userId, Long id, int quality) {
        LeetCodeCard card = get(userId, id);
        return SM2Scheduler.review(card, quality);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        if (!repository.existsByIdAndUserId(id, userId)) {
            throw new CardNotFoundException(id);
        }
        repository.deleteById(id);
    }
}