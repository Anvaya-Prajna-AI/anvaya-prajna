package ai.anvaya.prajna.api;

import ai.anvaya.prajna.api.dto.FeedbackRequest;
import ai.anvaya.prajna.api.dto.GenerateExplanationRequest;
import ai.anvaya.prajna.application.ExplanationService;
import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.ReasoningStep;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/explanations")
public class ExplanationController {

    private final ExplanationService explanationService;

    public ExplanationController(ExplanationService explanationService) {
        this.explanationService = explanationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ExplanationIR> generate(@Valid @RequestBody GenerateExplanationRequest request) {
        ExplanationIR ir = explanationService.generateExplanation(request.getQuestion(), request.getPolicy());
        return ResponseEntity.ok(ir);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<ExplanationIR> getExplanation(@PathVariable String questionId) {
        ExplanationIR ir = explanationService.getExplanationByQuestionId(questionId);
        return ResponseEntity.ok(ir);
    }

    @GetMapping("/{id}/steps/{stepId}")
    public ResponseEntity<ReasoningStep> getStep(
            @PathVariable UUID id, 
            @PathVariable String stepId) {
        ReasoningStep step = explanationService.getStep(id, stepId);
        return ResponseEntity.ok(step);
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<ExplanationIR> review(
            @PathVariable UUID id, 
            @RequestBody(required = false) ai.anvaya.prajna.api.dto.ReviewExplanationRequest request) {
        boolean approved = request == null || request.getApproved() == null || Boolean.TRUE.equals(request.getApproved());
        String comment = request != null ? request.getComment() : null;
        ExplanationIR ir = explanationService.reviewExplanation(id, approved, comment);
        return ResponseEntity.ok(ir);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ExplanationIR> publish(@PathVariable UUID id) {
        ExplanationIR ir = explanationService.publishExplanation(id);
        return ResponseEntity.ok(ir);
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<Void> submitFeedback(@PathVariable UUID id, @Valid @RequestBody FeedbackRequest request) {
        explanationService.submitFeedback(id, request.getStepId(), request.getUserId(), request.getFeedbackType(), request.getComment());
        return ResponseEntity.ok().build();
    }
}
