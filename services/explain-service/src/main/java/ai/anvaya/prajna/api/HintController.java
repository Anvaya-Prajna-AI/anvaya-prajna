package ai.anvaya.prajna.api;

import ai.anvaya.prajna.application.HintService;
import ai.anvaya.prajna.exception.ExplanationNotFoundException;
import ai.anvaya.prajna.ir.Hint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/explanations")
public class HintController {

    private final HintService hintService;

    public HintController(HintService hintService) {
        this.hintService = hintService;
    }

    @GetMapping("/{questionId}/hints")
    public ResponseEntity<Hint> getHint(@PathVariable String questionId,
                                        @RequestParam(defaultValue = "1") int level) {
        return hintService.getHint(questionId, level)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ExplanationNotFoundException("No hints found for question ID: " + questionId));
    }
}
