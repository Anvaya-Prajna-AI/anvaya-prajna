package ai.anvaya.prajna.validation;

import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.math.ExpressionEvaluator;
import ai.anvaya.prajna.reasoning.ReasoningProposal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnswerValidator implements ProposalValidator {

    private final ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator();

    @Override
    public String getName() {
        return "AnswerValidator";
    }

    @Override
    public ValidationResult validate(Question question, ReasoningProposal proposal) {
        if (question == null || question.getAuthoritativeAnswer() == null || question.getAuthoritativeAnswer().isBlank()) {
            return ValidationResult.builder().status(ValidationStatus.PASSED).score(1.0).build();
        }

        String expected = question.getAuthoritativeAnswer().trim();
        List<ValidationViolation> violations = new ArrayList<>();

        String actual = proposal != null && proposal.getConclusion() != null ? proposal.getConclusion().trim() : null;

        if (actual == null && proposal != null && proposal.getSteps() != null && !proposal.getSteps().isEmpty()) {
            ReasoningStep lastStep = proposal.getSteps().get(proposal.getSteps().size() - 1);
            actual = lastStep.getAfter() != null ? lastStep.getAfter().trim() : (lastStep.getOutputs() != null && !lastStep.getOutputs().isEmpty() ? lastStep.getOutputs().get(0) : null);
        }

        if (actual == null) {
            violations.add(ValidationViolation.builder()
                    .code("MISSING_ANSWER")
                    .message("Proposal did not produce a final answer/conclusion.")
                    .severity(ValidationSeverity.ERROR)
                    .build());
        } else {
            boolean matched = matchesAnswer(expected, actual);
            if (!matched) {
                violations.add(ValidationViolation.builder()
                        .code("ANSWER_MISMATCH")
                        .message("Proposed answer '" + actual + "' does not match authoritative answer '" + expected + "'.")
                        .severity(ValidationSeverity.ERROR)
                        .build());
            }
        }

        ValidationStatus status = violations.isEmpty() ? ValidationStatus.PASSED : ValidationStatus.FAILED;
        return ValidationResult.builder()
                .status(status)
                .score(violations.isEmpty() ? 1.0 : 0.0)
                .violations(violations)
                .build();
    }

    private boolean matchesAnswer(String expected, String actual) {
        if (expected.equalsIgnoreCase(actual)) {
            return true;
        }

        // Substring / word match
        if (actual.toLowerCase().contains(expected.toLowerCase())) {
            return true;
        }

        // Clean values like "x = 5" and "5"
        String expClean = expected.replaceAll("[^0-9a-zA-Z.-]", "").toLowerCase();
        String actClean = actual.replaceAll("[^0-9a-zA-Z.-]", "").toLowerCase();
        if (actClean.contains(expClean)) {
            return true;
        }

        // Numerical extraction comparison
        Matcher expNumMatcher = Pattern.compile("[-+]?\\d*\\.?\\d+").matcher(expected);
        Matcher actNumMatcher = Pattern.compile("[-+]?\\d*\\.?\\d+").matcher(actual);
        if (expNumMatcher.find() && actNumMatcher.find()) {
            try {
                double expNum = Double.parseDouble(expNumMatcher.group());
                double actNum = Double.parseDouble(actNumMatcher.group());
                return Math.abs(expNum - actNum) < 1e-5;
            } catch (Exception ignored) {}
        }

        return false;
    }
}
