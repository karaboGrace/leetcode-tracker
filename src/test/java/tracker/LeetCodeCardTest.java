package tracker;

import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LeetCodeCardTest {

    @Test
    void tagsCannotBeModifiedFromOutside() {
        LeetCodeCard card = new LeetCodeCard("Two Sum", "https://leetcode.com/problems/two-sum/",
                Difficulty.EASY, Set.of("array", "hash-map"));

        assertThrows(UnsupportedOperationException.class,
                () -> card.getTags().add("oops"));
    }
}