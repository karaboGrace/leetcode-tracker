package tracker;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService service;

    public CardController(CardService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CreateCardRequest request) {
        LeetCodeCard card = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/cards/" + card.getId()))
                .body(CardResponse.from(card));
    }

    @GetMapping
    public List<CardResponse> list(@RequestParam(defaultValue = "false") boolean dueOnly) {
        return service.list(dueOnly).stream().map(CardResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CardResponse get(@PathVariable Long id) {
        return CardResponse.from(service.get(id));
    }

    @PostMapping("/{id}/review")
    public CardResponse review(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return CardResponse.from(service.review(id, request.quality()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}