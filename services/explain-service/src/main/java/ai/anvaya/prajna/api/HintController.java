package ai.anvaya.prajna.api;

import ai.anvaya.prajna.application.HintService;
import ai.anvaya.prajna.exception.ExplanationNotFoundException;
import ai.anvaya.prajna.ir.Hint;
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
@Tag(name = "Hint API", description = "Endpoints for retrieving progressive scaffolding hints")
public class HintController {

    private final HintService hintService;

    public HintController(HintService hintService) {
        this.hintService = hintService;
    }

    @GetMapping("/{questionId}/hints")
    @Operation(summary = "Get Progressive Hint", description = "Retrieves a scaffolded hint for a given question and hint level (1=nudge, 2=strategy, 3=worked step)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hint found",
                    content = @Content(schema = @Schema(implementation = Hint.class))),
            @ApiResponse(responseCode = "404", description = "No hint found for question or level")
    })
    public ResponseEntity<Hint> getHint(
            @Parameter(description = "Question identifier", required = true) @PathVariable String questionId,
            @Parameter(description = "Progressive hint level (default: 1)", example = "1") @RequestParam(defaultValue = "1") int level) {
        return hintService.getHint(questionId, level)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ExplanationNotFoundException("No hints found for question ID: " + questionId));
    }
}
