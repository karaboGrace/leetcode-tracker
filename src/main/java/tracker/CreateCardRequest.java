package tracker;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateCardRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 500) String url,
        @NotNull Difficulty difficulty,
        Set<String> tags) {
}