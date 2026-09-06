package ai.anvaya.prajna.validation;

import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.math.MathValidator;
import ai.anvaya.prajna.reasoning.ReasoningProposal;

import java.util.ArrayList;
import java.util.List;

public class MathTransformationValidator implements ProposalValidator {

    private final MathValidator mathValidator;

    public MathTransformationValidator() {
        this.mathValidator = new MathValidator();
    }

    public MathTransformationValidator(MathValidator mathValidator) {
        this.mathValidator = mathValidator;
    }

    @Override
    public String getName() {
        return "MathTransformationValidator";
    }

    @Override
    public ValidationResult validate(Question question, ReasoningProposal proposal) {
        if (proposal == null || proposal.getSteps() == null) {
            return ValidationResult.builder().status(ValidationStatus.PASSED).score(1.0).build();
        }

        List<ValidationViolation> violations = new ArrayList<>();

        for (ReasoningStep step : proposal.getSteps()) {
            boolean valid = mathValidator.validateStep(step);
            if (!valid) {
                violations.add(ValidationViolation.builder()
                        .stepId(step.getId())
                        .code("INVALID_MATH_TRANSFORMATION")
                        .message("Mathematical operation or calculation in step " + step.getId() + " is invalid.")
                        .severity(ValidationSeverity.ERROR)
                        .build());
            }
        }

        if (proposal.getVerification() != null) {
            var verificationResult = mathValidator.validateVerificationResult(proposal.getVerification());
            if (!Boolean.TRUE.equals(verificationResult.getPassed())) {
                violations.add(ValidationViolation.builder()
                        .code("VERIFICATION_FAILED")
                        .message("Deterministic verification failed: " + verificationResult.getDetails())
                        .severity(ValidationSeverity.ERROR)
                        .build());
            }
        }

        ValidationStatus status = violations.isEmpty() ? ValidationStatus.PASSED : ValidationStatus.FAILED;
        double score = violations.isEmpty() ? 1.0 : Math.max(0.0, 1.0 - (violations.size() * 0.3));

        return ValidationResult.builder()
                .status(status)
                .score(score)
                .violations(violations)
                .build();
    }
}
