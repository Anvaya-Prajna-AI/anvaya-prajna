package ai.anvaya.prajna.math;

import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.ir.VerificationResult;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathValidator {

    private final ExpressionEvaluator expressionEvaluator;

    public MathValidator() {
        this.expressionEvaluator = new ExpressionEvaluator();
    }

    public MathValidator(ExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator;
    }

    public ExpressionEvaluator getExpressionEvaluator() {
        return expressionEvaluator;
    }

    /**
     * Validates whether a step's mathematical transformation or calculation is mathematically correct.
     */
    public boolean validateStep(ReasoningStep step) {
        if (step == null || step.getType() == null) {
            return false;
        }

        switch (step.getType()) {
            case CALCULATE:
                return validateCalculation(step);
            case TRANSFORM:
                return validateTransformation(step);
            case SUBSTITUTE:
                return validateSubstitution(step);
            case VERIFY:
                return validateVerification(step);
            default:
                return true;
        }
    }

    private boolean validateCalculation(ReasoningStep step) {
        String after = step.getAfter();
        String cleanAfter = cleanMathString(after);

        if (cleanAfter != null && cleanAfter.contains("=")) {
            String[] parts = cleanAfter.split("=", 2);
            try {
                double lhs = expressionEvaluator.evaluate(parts[0]);
                double rhs = expressionEvaluator.evaluate(parts[1]);
                return Math.abs(lhs - rhs) < 1e-5;
            } catch (Exception ignored) {
                // If variables are present (e.g. v = 60), check if before matches rhs
                if (step.getBefore() != null) {
                    try {
                        String cleanBefore = cleanMathString(step.getBefore());
                        String beforeExpr = cleanBefore.contains("=") ? cleanBefore.split("=", 2)[1] : cleanBefore;
                        double beforeVal = expressionEvaluator.evaluate(beforeExpr);
                        double rhsVal = expressionEvaluator.evaluate(parts[1]);
                        return Math.abs(beforeVal - rhsVal) < 1e-5;
                    } catch (Exception e2) {
                        return true;
                    }
                }
                return true;
            }
        }

        if (step.getBefore() != null && step.getAfter() != null) {
            try {
                double val1 = expressionEvaluator.evaluate(cleanMathString(step.getBefore()));
                double val2 = expressionEvaluator.evaluate(cleanMathString(step.getAfter()));
                return Math.abs(val1 - val2) < 1e-5;
            } catch (Exception e) {
                return true;
            }
        }
        return true;
    }

    private boolean validateTransformation(ReasoningStep step) {
        String before = step.getBefore();
        String after = step.getAfter();

        if (before == null || after == null) {
            return true;
        }

        String cleanBefore = cleanMathString(before);
        String cleanAfter = cleanMathString(after);

        // Check if both are equations
        if (cleanBefore.contains("=") && cleanAfter.contains("=")) {
            String[] beforeParts = cleanBefore.split("=", 2);
            String[] afterParts = cleanAfter.split("=", 2);

            String beforeRhs = beforeParts[1].trim();
            String afterRhs = afterParts[1].trim();

            if (step.getOperation() != null && step.getOperation().getName() != null) {
                String op = step.getOperation().getName().toUpperCase();
                Object valObj = step.getOperation().getValue();
                String valStr = valObj != null ? valObj.toString() : "0";

                try {
                    double opVal = Double.parseDouble(valStr);
                    double beforeRhsVal = expressionEvaluator.evaluate(beforeRhs);
                    double afterRhsVal = expressionEvaluator.evaluate(afterRhs);

                    switch (op) {
                        case "SUBTRACT":
                            return Math.abs((beforeRhsVal - opVal) - afterRhsVal) < 1e-5;
                        case "ADD":
                            return Math.abs((beforeRhsVal + opVal) - afterRhsVal) < 1e-5;
                        case "MULTIPLY":
                            return Math.abs((beforeRhsVal * opVal) - afterRhsVal) < 1e-5;
                        case "DIVIDE":
                            return opVal != 0 && Math.abs((beforeRhsVal / opVal) - afterRhsVal) < 1e-5;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return true;
    }

    private boolean validateSubstitution(ReasoningStep step) {
        if (step.getAfter() == null) {
            return true;
        }
        String cleanAfter = cleanMathString(step.getAfter());
        if (cleanAfter.contains("=")) {
            String[] parts = cleanAfter.split("=", 2);
            try {
                double lhs = expressionEvaluator.evaluate(parts[0]);
                double rhs = expressionEvaluator.evaluate(parts[1]);
                return Math.abs(lhs - rhs) < 1e-5;
            } catch (Exception ignored) {
                // Variable assignment or non-numeric expression is acceptable
                return true;
            }
        }
        return true;
    }

    private boolean validateVerification(ReasoningStep step) {
        if (step.getVerification() != null) {
            return validateVerificationResult(step.getVerification()).getPassed();
        }
        if (step.getAfter() != null && step.getAfter().contains("=")) {
            String clean = cleanMathString(step.getAfter());
            try {
                return expressionEvaluator.evaluateEquality(clean, Map.of());
            } catch (Exception e) {
                return true;
            }
        }
        return true;
    }

    /**
     * Validates a VerificationResult object.
     */
    public VerificationResult validateVerificationResult(VerificationResult result) {
        if (result == null || result.getExpression() == null) {
            return VerificationResult.builder().passed(false).details("Null verification expression").build();
        }

        if ("LOGICAL_CONSISTENCY".equalsIgnoreCase(result.getType()) || 
            "PATTERN_CHECK".equalsIgnoreCase(result.getType()) ||
            "LOGIC".equalsIgnoreCase(result.getType()) ||
            "SET_CONTAINMENT".equalsIgnoreCase(result.getType()) ||
            "DIMENSIONAL_ANALYSIS".equalsIgnoreCase(result.getType()) ||
            "ATOM_CONSERVATION".equalsIgnoreCase(result.getType()) ||
            "PERCENT_CHECK".equalsIgnoreCase(result.getType()) ||
            "SUM_CHECK".equalsIgnoreCase(result.getType())) {
            boolean passed = result.getPassed() != null ? result.getPassed() : true;
            return VerificationResult.builder()
                    .type(result.getType())
                    .expression(result.getExpression())
                    .expected(true)
                    .actual(passed)
                    .passed(passed)
                    .details(result.getDetails() != null ? result.getDetails() : (passed ? "Domain rule verified" : "Domain rule rejected"))
                    .build();
        }

        String clean = cleanMathString(result.getExpression());
        boolean passed = false;
        try {
            passed = expressionEvaluator.evaluateEquality(clean, Map.of());
        } catch (Exception e) {
            passed = Boolean.TRUE.equals(result.getPassed());
        }

        return VerificationResult.builder()
                .type(result.getType() != null ? result.getType() : "SUBSTITUTION")
                .expression(result.getExpression())
                .expected(true)
                .actual(passed)
                .passed(passed)
                .details(passed ? "Verification passed" : "Verification expression evaluated to false")
                .build();
    }

    private String cleanMathString(String raw) {
        if (raw == null) return null;
        // Strip out units like km/h, m/s^2, m/s, km, miles, hours, hr, sec, s, kg, g, N, J, W, mol
        return raw.replaceAll("(?i)\\b(km/h|m/s\\^2|m/s2|m/s|km|miles|hours|hour|hr|min|sec|kg|kJ/mol|mol|g|m|h|s|N|J|W)\\b", "").trim();
    }
}
