package ai.anvaya.prajna.validation;

import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.reasoning.ReasoningProposal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogicValidator implements ProposalValidator {

    @Override
    public String getName() {
        return "LogicValidator";
    }

    @Override
    public ValidationResult validate(Question question, ReasoningProposal proposal) {
        if (proposal == null || proposal.getSteps() == null) {
            return ValidationResult.builder().status(ValidationStatus.PASSED).score(1.0).build();
        }

        List<ValidationViolation> violations = new ArrayList<>();
        Set<String> declaredIds = new HashSet<>();

        for (ReasoningStep step : proposal.getSteps()) {
            if (step.getId() != null) {
                if (declaredIds.contains(step.getId())) {
                    violations.add(ValidationViolation.builder()
                            .stepId(step.getId())
                            .code("DUPLICATE_STEP_ID")
                            .message("Duplicate step ID: " + step.getId())
                            .severity(ValidationSeverity.ERROR)
                            .build());
                }
                declaredIds.add(step.getId());
            }

            // Verify inputs reference known previous steps or facts
            if (step.getInputs() != null) {
                for (String input : step.getInputs()) {
                    if (!declaredIds.contains(input) && !input.startsWith("f") && !input.equals(step.getId())) {
                        violations.add(ValidationViolation.builder()
                                .stepId(step.getId())
                                .code("DANGLING_INPUT_REFERENCE")
                                .message("Step " + step.getId() + " references undefined input: " + input)
                                .severity(ValidationSeverity.WARNING)
                                .build());
                    }
                }
            }
        }

        ValidationStatus status = violations.stream().anyMatch(v -> v.getSeverity() == ValidationSeverity.ERROR)
                ? ValidationStatus.FAILED : ValidationStatus.PASSED;

        return ValidationResult.builder()
                .status(status)
                .score(status == ValidationStatus.PASSED ? 1.0 : 0.5)
                .violations(violations)
                .build();
    }
}
