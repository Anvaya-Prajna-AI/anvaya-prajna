package ai.anvaya.prajna.math;

import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepOperation;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.ir.VerificationResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MathValidatorTest {

    private final MathValidator validator = new MathValidator();

    @Test
    void shouldEvaluateArithmeticExpressions() {
        double result = validator.getExpressionEvaluator().evaluate("3 * 5 + 5");
        assertThat(result).isEqualTo(20.0);
    }

    @Test
    void shouldCheckEquality() {
        boolean equal = validator.getExpressionEvaluator().evaluateEquality("3 * 5 + 5 = 20", null);
        assertThat(equal).isTrue();
    }

    @Test
    void shouldValidateTransformStep() {
        ReasoningStep step = ReasoningStep.builder()
                .id("s1")
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder()
                        .name("SUBTRACT")
                        .target("both-sides")
                        .value(5)
                        .build())
                .before("3x + 5 = 20")
                .after("3x = 15")
                .build();

        boolean valid = validator.validateStep(step);
        assertThat(valid).isTrue();

        // Test ADD
        ReasoningStep addStep = ReasoningStep.builder()
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder().name("ADD").value(4).build())
                .before("x - 4 = 10")
                .after("x = 14")
                .build();
        assertThat(validator.validateStep(addStep)).isTrue();

        // Test MULTIPLY
        ReasoningStep multStep = ReasoningStep.builder()
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder().name("MULTIPLY").value(2).build())
                .before("x / 2 = 5")
                .after("x = 10")
                .build();
        assertThat(validator.validateStep(multStep)).isTrue();

        // Test DIVIDE
        ReasoningStep divStep = ReasoningStep.builder()
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder().name("DIVIDE").value(3).build())
                .before("3x = 15")
                .after("x = 5")
                .build();
        assertThat(validator.validateStep(divStep)).isTrue();
    }

    @Test
    void shouldValidateCalculateAndSubstituteSteps() {
        ReasoningStep calcStep = ReasoningStep.builder()
                .type(StepType.CALCULATE)
                .before("3 * 5")
                .after("15")
                .build();
        assertThat(validator.validateStep(calcStep)).isTrue();

        ReasoningStep subStep = ReasoningStep.builder()
                .type(StepType.SUBSTITUTE)
                .after("3(5) + 5 = 20")
                .build();
        assertThat(validator.validateStep(subStep)).isTrue();

        ReasoningStep verifyStep = ReasoningStep.builder()
                .type(StepType.VERIFY)
                .after("20 = 20")
                .build();
        assertThat(validator.validateStep(verifyStep)).isTrue();

        assertThat(validator.validateStep(null)).isFalse();
        assertThat(validator.validateStep(ReasoningStep.builder().type(StepType.GIVEN).build())).isTrue();
    }

    @Test
    void shouldValidateVerificationResult() {
        VerificationResult vr = VerificationResult.builder()
                .type("SUBSTITUTION")
                .expression("3 * 5 + 5 = 20")
                .passed(true)
                .build();

        VerificationResult validated = validator.validateVerificationResult(vr);
        assertThat(validated.getPassed()).isTrue();

        // Null expression
        assertThat(validator.validateVerificationResult(null).getPassed()).isFalse();

        // Logic domain types
        VerificationResult logicVr = VerificationResult.builder()
                .type("LOGICAL_CONSISTENCY")
                .expression("All Greeks mortal")
                .passed(true)
                .build();
        assertThat(validator.validateVerificationResult(logicVr).getPassed()).isTrue();
    }
}
