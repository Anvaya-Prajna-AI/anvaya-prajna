package ai.anvaya.prajna.math;

import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepOperation;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.ir.VerificationResult;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class MathValidatorTest {

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final MathValidator validator = new MathValidator(evaluator);

    @Test
    void shouldEvaluateArithmeticExpressions() {
        assertThat(evaluator.evaluate("3 * 5 + 5")).isCloseTo(20.0, offset(1e-6));
        assertThat(evaluator.evaluate("120 / 2")).isCloseTo(60.0, offset(1e-6));
        assertThat(evaluator.evaluate("3x + 5", Map.of("x", 5.0))).isCloseTo(20.0, offset(1e-6));
    }

    @Test
    void shouldCheckEquality() {
        assertThat(evaluator.checkEquality("3x + 5", "5 + 3x", Set.of("x"))).isTrue();
        assertThat(evaluator.evaluateEquality("3 * 5 + 5 = 20", Map.of())).isTrue();
    }

    @Test
    void shouldValidateTransformStep() {
        ReasoningStep step = ReasoningStep.builder()
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder().name("SUBTRACT").target("both-sides").value(5).build())
                .before("3x + 5 = 20")
                .after("3x = 15")
                .build();

        assertThat(validator.validateStep(step)).isTrue();
    }

    @Test
    void shouldValidateVerificationResult() {
        VerificationResult verification = VerificationResult.builder()
                .type("SUBSTITUTION")
                .expression("3 * 5 + 5 = 20")
                .build();

        VerificationResult validated = validator.validateVerificationResult(verification);
        assertThat(validated.getPassed()).isTrue();
    }
}
