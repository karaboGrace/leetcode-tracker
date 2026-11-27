package tracker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvImporterTest {

    @Mock
    private CardRepository repository;

    @TempDir
    Path tempDir;

    @Test
    void importsValidRowsAndSkipsMalformedOnes() throws Exception {
        Path csv = tempDir.resolve("cards.csv");
        Files.writeString(csv, String.join("\n",
                "id,title,url,difficulty,tags,easinessFactor,repetitions,intervalDays,dueDate,lastReviewed",
                "1,Two Sum,https://leetcode.com/problems/two-sum/,EASY,array;hash-map,2.6,1,1,2026-11-27,2026-11-26",
                "2,Bad, Title,https://leetcode.com/problems/bad/,HARD,dp,2.5,0,0,2026-11-27,",
                "3,Coin Change,https://leetcode.com/problems/coin-change/,MEDIUM,dp,2.5,0,0,2026-11-27,"));
        when(repository.existsByUrl(any())).thenReturn(false);
        when(repository.save(any(LeetCodeCard.class))).thenAnswer(inv -> inv.getArgument(0));

        new CsvImporter(repository, "").importFile(csv);

        ArgumentCaptor<LeetCodeCard> saved = ArgumentCaptor.forClass(LeetCodeCard.class);
        verify(repository, times(2)).save(saved.capture());

        LeetCodeCard first = saved.getAllValues().get(0);
        assertEquals("Two Sum", first.getTitle());
        assertEquals(2.6, first.getEasinessFactor(), 1e-9);
        assertEquals(Set.of("array", "hash-map"), first.getTags());
        assertEquals(LocalDate.of(2026, 11, 26), first.getLastReviewed());

        LeetCodeCard second = saved.getAllValues().get(1);
        assertEquals("Coin Change", second.getTitle());
        assertNull(second.getLastReviewed());
    }
}