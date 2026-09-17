package ai.anvaya.prajna.math;

import ai.anvaya.prajna.ir.MathExpression;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MathAstBuilderTest {

    private final MathAstBuilder builder = new MathAstBuilder();

    @Test
    void shouldBuildFullMathExpression() {
        MathExpression expr = builder.build("3*x + 5 = 20");
        assertThat(expr).isNotNull();
        assertThat(expr.getRawText()).isEqualTo("3*x + 5 = 20");
        assertThat(expr.getLatex()).contains("\\cdot");
        assertThat(expr.getMathml()).contains("<math");
        assertThat(expr.getSemanticAst().get("type").asText()).isEqualTo("equation");
    }

    @Test
    void shouldHandleNullAndFractions() {
        assertThat(builder.build(null)).isNull();
        assertThat(builder.toLatex("a / b")).isEqualTo("\\frac{a}{b}");
        assertThat(builder.toLatex("x^2")).isEqualTo("x^{2}");
        assertThat(builder.toLatex(null)).isEmpty();
        assertThat(builder.toMathML(null)).isEmpty();

        ObjectNode node = builder.buildSimpleAst("42");
        assertThat(node.get("type").asText()).isEqualTo("expression");
        assertThat(node.get("raw").asText()).isEqualTo("42");

        ObjectNode nullNode = builder.buildSimpleAst(null);
        assertThat(nullNode.get("type").asText()).isEqualTo("null");
    }
}
