package ai.anvaya.prajna.api;

import ai.anvaya.prajna.application.WhyService;
import ai.anvaya.prajna.exception.ExplanationNotFoundException;
import ai.anvaya.prajna.ir.StepJustification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/explanations")
@Tag(name = "Why API", description = "Endpoints for retrieving step-level pedagogical and mathematical justifications")
public class WhyController {

    private final WhyService whyService;

    public WhyController(WhyService whyService) {
        this.whyService = whyService;
    }

    @GetMapping("/{questionId}/why")
    @Operation(summary = "Get Step Justification", description = "Returns deep pedagogical justification and underlying mathematical theorem for a specific step")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Justification found",
                    content = @Content(schema = @Schema(implementation = StepJustification.class))),
            @ApiResponse(responseCode = "404", description = "Step justification not found")
    })
    public ResponseEntity<StepJustification> getWhy(
            @Parameter(description = "Question identifier", required = true) @PathVariable String questionId,
            @Parameter(description = "Step identifier", required = true) @RequestParam String stepId) {
        return whyService.getWhyForStep(questionId, stepId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ExplanationNotFoundException("Step justification not found for step " + stepId + " on question " + questionId));
    }
}
