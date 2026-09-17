package ai.anvaya.prajna.math;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionEvaluatorTest {

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    @Test
    void shouldNormalizeLatexAndImplicitMultiplication() {
        assertThat(evaluator.normalizeExpression("3 \\times 4")).isEqualTo("3 * 4");
        assertThat(evaluator.normalizeExpression("10 \\div 2")).isEqualTo("10 / 2");
        assertThat(evaluator.normalizeExpression("5 \\cdot 6")).isEqualTo("5 * 6");
        assertThat(evaluator.normalizeExpression("3x")).isEqualTo("3*x");
        assertThat(evaluator.normalizeExpression("3(5)")).isEqualTo("3*(5)");
        assertThat(evaluator.normalizeExpression("(x)(y)")).isEqualTo("(x)*(y)");
        assertThat(evaluator.normalizeExpression(null)).isEmpty();
    }

    @Test
    void shouldEvaluateNumericExpressions() {
        assertThat(evaluator.evaluate("2 + 3 * 4")).isEqualTo(14.0);
        assertThat(evaluator.evaluate("(10 - 4) / 2")).isEqualTo(3.0);
        assertThat(evaluator.evaluate("3 * x + 5", Map.of("x", 5.0))).isEqualTo(20.0);
    }

    @Test
    void shouldThrowOnEmptyExpression() {
        assertThatThrownBy(() -> evaluator.evaluate(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCheckEqualityAcrossVariables() {
        assertThat(evaluator.checkEquality("2*x + 3*x", "5*x", Set.of("x"))).isTrue();
        assertThat(evaluator.checkEquality("x + 1", "x + 2", Set.of("x"))).isFalse();
        assertThat(evaluator.checkEquality("4 + 6", "10", Set.of())).isTrue();
        assertThat(evaluator.checkEquality(null, "10", Set.of())).isFalse();
    }

    @Test
    void shouldEvaluateEqualityStrings() {
        assertThat(evaluator.evaluateEquality("3 * 5 + 5 = 20", Map.of())).isTrue();
        assertThat(evaluator.evaluateEquality("3 * x + 5 = 20", Map.of("x", 5.0))).isTrue();
        assertThat(evaluator.evaluateEquality("3 * x + 5 = 20", Map.of("x", 4.0))).isFalse();
        assertThat(evaluator.evaluateEquality("invalid", Map.of())).isFalse();
        assertThat(evaluator.evaluateEquality(null, Map.of())).isFalse();
    }
}
