package ai.anvaya.prajna.validation;

import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.reasoning.ReasoningProposal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SafetyValidator implements ProposalValidator {

    private static final List<Pattern> FORBIDDEN_PATTERNS = List.of(
            Pattern.compile("<script[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("data:text/html", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onerror\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE)
    );

    @Override
    public String getName() {
        return "SafetyValidator";
    }

    @Override
    public ValidationResult validate(Question question, ReasoningProposal proposal) {
        List<ValidationViolation> violations = new ArrayList<>();

        if (proposal != null) {
            checkText(proposal.getProblemSummary(), null, violations);
            checkText(proposal.getConclusion(), null, violations);

            if (proposal.getSteps() != null) {
                for (ReasoningStep step : proposal.getSteps()) {
                    String stepId = step.getId();
                    checkText(step.getBefore(), stepId, violations);
                    checkText(step.getAfter(), stepId, violations);
                    if (step.getJustification() != null) {
                        checkText(step.getJustification().getText(), stepId, violations);
                    }
                }
            }
        }

        ValidationStatus status = violations.isEmpty() ? ValidationStatus.PASSED : ValidationStatus.FAILED;
        return ValidationResult.builder()
                .status(status)
                .score(violations.isEmpty() ? 1.0 : 0.0)
                .violations(violations)
                .build();
    }

    private void checkText(String text, String stepId, List<ValidationViolation> violations) {
        if (text == null) return;
        for (Pattern pattern : FORBIDDEN_PATTERNS) {
            if (pattern.matcher(text).find()) {
                violations.add(ValidationViolation.builder()
                        .stepId(stepId)
                        .code("SECURITY_VIOLATION")
                        .message("Forbidden unsafe pattern detected: " + pattern.pattern())
                        .severity(ValidationSeverity.ERROR)
                        .build());
            }
        }
    }
}
