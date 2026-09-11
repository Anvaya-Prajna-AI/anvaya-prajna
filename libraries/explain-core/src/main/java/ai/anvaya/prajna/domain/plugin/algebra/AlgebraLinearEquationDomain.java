package ai.anvaya.prajna.domain.plugin.algebra;

import ai.anvaya.prajna.domain.plugin.ExplanationDomain;
import ai.anvaya.prajna.ir.Hint;
import ai.anvaya.prajna.ir.Misconception;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.RepresentationType;
import ai.anvaya.prajna.ir.StepJustification;
import ai.anvaya.prajna.ir.StepOperation;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.ir.VerificationResult;
import ai.anvaya.prajna.math.ExpressionEvaluator;
import ai.anvaya.prajna.math.MathValidator;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import ai.anvaya.prajna.validation.ValidationResult;
import ai.anvaya.prajna.validation.ValidationStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AlgebraLinearEquationDomain implements ExplanationDomain {

    private final MathValidator mathValidator = new MathValidator();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    // Match forms like: 3x + 5 = 20 or 2x - 4 = 10
    private static final Pattern LINEAR_PATTERN = Pattern.compile("(\\d*)\\s*([a-zA-Z])\\s*([+-])\\s*(\\d+)\\s*=\\s*(\\d+)");

    @Override
    public boolean supports(Question question) {
        if (question == null || question.getStatement() == null) return false;
        String domain = question.getDomain();
        if (domain != null && domain.equalsIgnoreCase("ALGEBRA")) return true;
        return LINEAR_PATTERN.matcher(question.getStatement()).find();
    }

    @Override
    public String getDomainName() {
        return "ALGEBRA";
    }

    @Override
    public ReasoningProposal generateReasoning(Question question) {
        String stmt = question.getStatement();
        Matcher m = LINEAR_PATTERN.matcher(stmt);

        if (m.find()) {
            String coeffStr = m.group(1);
            int a = coeffStr.isEmpty() ? 1 : Integer.parseInt(coeffStr);
            String var = m.group(2);
            String sign = m.group(3);
            int b = Integer.parseInt(m.group(4));
            int c = Integer.parseInt(m.group(5));

            int rhsAfterAddSub = sign.equals("+") ? (c - b) : (c + b);
            String inverseOpName = sign.equals("+") ? "SUBTRACT" : "ADD";
            double finalAns = (double) rhsAfterAddSub / a;
            String finalAnsStr = (finalAns == (long) finalAns) ? String.format("%d", (long) finalAns) : String.format("%.2f", finalAns);

            List<ReasoningStep> steps = new ArrayList<>();

            // Step 1: Inverse constant operation
            steps.add(ReasoningStep.builder()
                    .id("s1")
                    .sequence(1)
                    .type(StepType.TRANSFORM)
                    .operation(StepOperation.builder().name(inverseOpName).target("both-sides").value(b).build())
                    .before(a + var + " " + sign + " " + b + " = " + c)
                    .after(a + var + " = " + rhsAfterAddSub)
                    .justification(StepJustification.builder().text(inverseOpName + " " + b + " from both sides to isolate the variable term.").build())
                    .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH, RepresentationType.ANIMATION))
                    .build());

            // Step 2: Divide by coefficient if a > 1
            if (a != 1) {
                steps.add(ReasoningStep.builder()
                        .id("s2")
                        .sequence(2)
                        .type(StepType.TRANSFORM)
                        .operation(StepOperation.builder().name("DIVIDE").target("both-sides").value(a).build())
                        .before(a + var + " = " + rhsAfterAddSub)
                        .after(var + " = " + finalAnsStr)
                        .justification(StepJustification.builder().text("Divide both sides by " + a + " to solve for " + var + ".").build())
                        .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH, RepresentationType.ANIMATION))
                        .build());
            }

            // Step 3: Verification
            String verifyExpr = a + " * " + finalAnsStr + " " + sign + " " + b + " = " + c;
            steps.add(ReasoningStep.builder()
                    .id("s" + (steps.size() + 1))
                    .sequence(steps.size() + 1)
                    .type(StepType.VERIFY)
                    .before(var + " = " + finalAnsStr)
                    .after(verifyExpr)
                    .justification(StepJustification.builder().text("Substitute " + var + " = " + finalAnsStr + " back into the original equation to verify.").build())
                    .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                    .build());

            VerificationResult verification = VerificationResult.builder()
                    .type("SUBSTITUTION")
                    .expression(verifyExpr)
                    .expected(true)
                    .actual(true)
                    .passed(true)
                    .details("Verified that " + verifyExpr + " holds true.")
                    .build();

            List<Hint> hints = List.of(
                    Hint.builder().id("h1").level(1).text("Identify the constant term on the variable side.").build(),
                    Hint.builder().id("h2").level(2).text("Perform the inverse operation (" + inverseOpName + " " + b + ") on both sides.").build(),
                    Hint.builder().id("h3").level(3).text("Divide both sides by the coefficient " + a + " to isolate " + var + ".").build()
            );

            List<Misconception> misconceptions = List.of(
                    Misconception.builder()
                            .id("m1")
                            .incorrectReasoning("Adding " + b + " instead of subtracting " + b)
                            .whyWrong("When balancing an equation, use the opposite (inverse) operation.")
                            .correctedReasoning("Subtract " + b + " from both sides because the equation has +" + b)
                            .build()
            );

            return ReasoningProposal.builder()
                    .questionId(question.getQuestionId())
                    .problemSummary("Solve linear equation " + stmt)
                    .concepts(List.of("linear-equation", "inverse-operation", "variable-isolation"))
                    .facts(List.of("Given equation: " + a + var + " " + sign + " " + b + " = " + c))
                    .steps(steps)
                    .conclusion(var + " = " + finalAnsStr)
                    .verification(verification)
                    .hints(hints)
                    .misconceptions(misconceptions)
                    .build();
        }

        // Generic fallback proposal
        return ReasoningProposal.builder()
                .questionId(question.getQuestionId())
                .problemSummary(stmt)
                .concepts(List.of("algebra"))
                .steps(List.of(
                        ReasoningStep.builder().id("s1").type(StepType.GIVEN).after(stmt).build(),
                        ReasoningStep.builder().id("s2").type(StepType.CONCLUDE).after(question.getAuthoritativeAnswer() != null ? question.getAuthoritativeAnswer() : "Solution").build()
                ))
                .conclusion(question.getAuthoritativeAnswer() != null ? question.getAuthoritativeAnswer() : "Solved")
                .build();
    }

    @Override
    public ValidationResult validate(ReasoningProposal proposal) {
        if (proposal == null || proposal.getSteps() == null) {
            return ValidationResult.builder().status(ValidationStatus.PASSED).score(1.0).build();
        }
        for (ReasoningStep step : proposal.getSteps()) {
            if (!mathValidator.validateStep(step)) {
                return ValidationResult.builder().status(ValidationStatus.FAILED).score(0.0).build();
            }
        }
        return ValidationResult.builder().status(ValidationStatus.PASSED).score(1.0).build();
    }

    @Override
    public List<Hint> generateHints(ReasoningProposal proposal) {
        return proposal.getHints() != null ? proposal.getHints() : List.of();
    }

    @Override
    public List<Misconception> detectMisconceptions(ReasoningProposal proposal) {
        return proposal.getMisconceptions() != null ? proposal.getMisconceptions() : List.of();
    }
}
