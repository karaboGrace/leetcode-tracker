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
    public LeetCodeCard create(CreateCardRequest request) {
        Set<String> tags = request.tags() == null ? Set.of() : request.tags();
        return repository.save(new LeetCodeCard(
                request.title(), request.url(), request.difficulty(), tags));
    }

    @Transactional(readOnly = true)
    public List<LeetCodeCard> list(boolean dueOnly) {
        if (dueOnly) {
            return repository.findByDueDateLessThanEqualOrderByDueDateAsc(LocalDate.now());
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public LeetCodeCard get(Long id) {
        return repository.findById(id).orElseThrow(() -> new CardNotFoundException(id));
    }

    @Transactional
    public LeetCodeCard review(Long id, int quality) {
        LeetCodeCard card = get(id);
        return SM2Scheduler.review(card, quality);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CardNotFoundException(id);
        }
        repository.deleteById(id);
    }
}