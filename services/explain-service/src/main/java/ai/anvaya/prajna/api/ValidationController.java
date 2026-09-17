package ai.anvaya.prajna.api;

import ai.anvaya.prajna.api.dto.ValidateProposalRequest;
import ai.anvaya.prajna.application.ValidationService;
import ai.anvaya.prajna.validation.ValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/explanations")
@Tag(name = "Validation API", description = "Endpoints for independently validating reasoning proposals against AST, math, logic, and schema constraints")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate Reasoning Proposal", description = "Executes the 4-phase validation pipeline (AST check, math verification, logic soundess, schema validation) on a reasoning proposal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validation result produced",
                    content = @Content(schema = @Schema(implementation = ValidationResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public ResponseEntity<ValidationResult> validate(@Valid @RequestBody ValidateProposalRequest request) {
        ValidationResult result = validationService.validateProposal(request.getQuestion(), request.getProposal());
        return ResponseEntity.ok(result);
    }
}
