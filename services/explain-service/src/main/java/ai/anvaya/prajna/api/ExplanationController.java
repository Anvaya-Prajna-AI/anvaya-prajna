package ai.anvaya.prajna.api;

import ai.anvaya.prajna.api.dto.FeedbackRequest;
import ai.anvaya.prajna.api.dto.GenerateExplanationRequest;
import ai.anvaya.prajna.api.dto.ReviewExplanationRequest;
import ai.anvaya.prajna.application.ExplanationService;
import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.ReasoningStep;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Explanation API", description = "Endpoints for generating, retrieving, reviewing, and publishing explanations")
public class ExplanationController {

    private final ExplanationService explanationService;

    public ExplanationController(ExplanationService explanationService) {
        this.explanationService = explanationService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate Explanation IR", description = "Orchestrates multi-agent reasoning, domain verification, and compilation into structured Explanation IR")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Explanation generated successfully",
                    content = @Content(schema = @Schema(implementation = ExplanationIR.class))),
            @ApiResponse(responseCode = "400", description = "Invalid question format or validation failure"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    @RateLimiter(name = "explanationGeneration")
    public ResponseEntity<ExplanationIR> generate(@Valid @RequestBody GenerateExplanationRequest request) {
        ExplanationIR ir = explanationService.generateExplanation(request.getQuestion(), request.getPolicy());
        return ResponseEntity.ok(ir);
    }

    @GetMapping("/{questionId}")
    @Operation(summary = "Get Explanation by Question ID", description = "Fetches the published or current explanation for a given question ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Explanation found",
                    content = @Content(schema = @Schema(implementation = ExplanationIR.class))),
            @ApiResponse(responseCode = "404", description = "Explanation not found")
    })
    public ResponseEntity<ExplanationIR> getExplanation(
            @Parameter(description = "Question identifier", required = true) @PathVariable String questionId) {
        ExplanationIR ir = explanationService.getExplanationByQuestionId(questionId);
        return ResponseEntity.ok(ir);
    }

    @GetMapping("/{id}/steps/{stepId}")
    @Operation(summary = "Get Reasoning Step", description = "Fetches an individual step within an explanation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Step found",
                    content = @Content(schema = @Schema(implementation = ReasoningStep.class))),
            @ApiResponse(responseCode = "404", description = "Explanation or step not found")
    })
    public ResponseEntity<ReasoningStep> getStep(
            @Parameter(description = "Explanation UUID", required = true) @PathVariable UUID id,
            @Parameter(description = "Step identifier", required = true) @PathVariable String stepId) {
        ReasoningStep step = explanationService.getStep(id, stepId);
        return ResponseEntity.ok(step);
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Review Explanation", description = "Allows educators to approve, reject, or comment on a draft explanation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review applied successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires EDUCATOR or ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Explanation not found")
    })
    public ResponseEntity<ExplanationIR> review(
            @Parameter(description = "Explanation UUID", required = true) @PathVariable UUID id,
            @RequestBody(required = false) ReviewExplanationRequest request) {
        boolean approved = request == null || request.getApproved() == null || Boolean.TRUE.equals(request.getApproved());
        String comment = request != null ? request.getComment() : null;
        ExplanationIR ir = explanationService.reviewExplanation(id, approved, comment);
        return ResponseEntity.ok(ir);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish Explanation", description = "Marks a reviewed explanation as published for student consumption")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Explanation published successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Explanation not found")
    })
    public ResponseEntity<ExplanationIR> publish(
            @Parameter(description = "Explanation UUID", required = true) @PathVariable UUID id) {
        ExplanationIR ir = explanationService.publishExplanation(id);
        return ResponseEntity.ok(ir);
    }

    @PostMapping("/{id}/feedback")
    @Operation(summary = "Submit Feedback", description = "Submits feedback on the explanation or specific reasoning step")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feedback recorded"),
            @ApiResponse(responseCode = "404", description = "Explanation not found")
    })
    public ResponseEntity<Void> submitFeedback(
            @Parameter(description = "Explanation UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody FeedbackRequest request) {
        explanationService.submitFeedback(id, request.getStepId(), request.getUserId(), request.getFeedbackType(), request.getComment());
        return ResponseEntity.ok().build();
    }
}
