package tracker;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CardRepository extends JpaRepository<LeetCodeCard, Long> {

    List<LeetCodeCard> findByDueDateLessThanEqualOrderByDueDateAsc(LocalDate date);
}