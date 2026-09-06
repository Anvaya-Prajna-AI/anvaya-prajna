package ai.anvaya.prajna.validation;

import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.reasoning.ReasoningProposal;

import java.util.ArrayList;
import java.util.List;

public class ValidationPipeline {

    private final List<ProposalValidator> validators;

    public ValidationPipeline() {
        this.validators = new ArrayList<>(List.of(
                new SafetyValidator(),
                new LogicValidator(),
                new MathTransformationValidator(),
                new AnswerValidator()
        ));
    }

    public ValidationPipeline(List<ProposalValidator> customValidators) {
        this.validators = new ArrayList<>(customValidators);
    }

    public void addValidator(ProposalValidator validator) {
        this.validators.add(validator);
    }

    public ValidationResult validate(Question question, ReasoningProposal proposal) {
        List<ValidationViolation> allViolations = new ArrayList<>();
        boolean anyError = false;
        double totalScore = 0.0;

        for (ProposalValidator validator : validators) {
            ValidationResult result = validator.validate(question, proposal);
            if (result.getViolations() != null) {
                allViolations.addAll(result.getViolations());
            }
            if (result.getStatus() == ValidationStatus.FAILED) {
                anyError = true;
            }
            totalScore += result.getScore() != null ? result.getScore() : 1.0;
        }

        double averageScore = validators.isEmpty() ? 1.0 : totalScore / validators.size();
        ValidationStatus overallStatus = anyError ? ValidationStatus.FAILED :
                (allViolations.isEmpty() ? ValidationStatus.PASSED : ValidationStatus.WARNING);

        return ValidationResult.builder()
                .status(overallStatus)
                .score(averageScore)
                .violations(allViolations)
                .build();
    }
}
