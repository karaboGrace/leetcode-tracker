package tracker;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<LeetCodeCard, Long> {

    List<LeetCodeCard> findByUserId(Long userId);

    List<LeetCodeCard> findByUserIdAndDueDateLessThanEqualOrderByDueDateAsc(Long userId, LocalDate date);

    Optional<LeetCodeCard> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    boolean existsByUrl(String url);
}