package tracker;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    // The user id comes from the verified token's subject claim, never from the request body.
    private static Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    @PostMapping
    public ResponseEntity<CardResponse> create(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody CreateCardRequest request) {
        LeetCodeCard card = service.create(userId(jwt), request);
        return ResponseEntity
                .created(URI.create("/api/cards/" + card.getId()))
                .body(CardResponse.from(card));
    }

    @GetMapping
    public List<CardResponse> list(@AuthenticationPrincipal Jwt jwt,
                                   @RequestParam(defaultValue = "false") boolean dueOnly) {
        return service.list(userId(jwt), dueOnly).stream().map(CardResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CardResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return CardResponse.from(service.get(userId(jwt), id));
    }

    @PostMapping("/{id}/review")
    public CardResponse review(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                               @Valid @RequestBody ReviewRequest request) {
        return CardResponse.from(service.review(userId(jwt), id, request.quality()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        service.delete(userId(jwt), id);
    }
}