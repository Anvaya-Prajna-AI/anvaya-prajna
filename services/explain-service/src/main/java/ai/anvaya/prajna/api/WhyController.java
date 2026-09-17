package ai.anvaya.prajna.api;

import ai.anvaya.prajna.application.WhyService;
import ai.anvaya.prajna.exception.ExplanationNotFoundException;
import ai.anvaya.prajna.ir.StepJustification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/explanations")
public class WhyController {

    private final WhyService whyService;

    public WhyController(WhyService whyService) {
        this.whyService = whyService;
    }

    @GetMapping("/{questionId}/why")
    public ResponseEntity<StepJustification> getWhy(@PathVariable String questionId,
                                                    @RequestParam String stepId) {
        return whyService.getWhyForStep(questionId, stepId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ExplanationNotFoundException("Step justification not found for step " + stepId + " on question " + questionId));
    }
}
