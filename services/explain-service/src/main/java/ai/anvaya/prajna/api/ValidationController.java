package ai.anvaya.prajna.api;

import ai.anvaya.prajna.api.dto.ValidateProposalRequest;
import ai.anvaya.prajna.application.ValidationService;
import ai.anvaya.prajna.validation.ValidationResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/explanations")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validate(@Valid @RequestBody ValidateProposalRequest request) {
        ValidationResult result = validationService.validateProposal(request.getQuestion(), request.getProposal());
        return ResponseEntity.ok(result);
    }
}
