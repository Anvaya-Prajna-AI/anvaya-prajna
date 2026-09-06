package ai.anvaya.prajna.math;

import ai.anvaya.prajna.ir.MathExpression;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MathAstBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Builds a MathExpression from a plain string or LaTeX formula.
     */
    public MathExpression build(String expression) {
        if (expression == null) {
            return null;
        }

        String latex = toLatex(expression);
        String mathml = toMathML(expression);
        ObjectNode ast = buildSimpleAst(expression);

        return MathExpression.builder()
                .rawText(expression)
                .latex(latex)
                .mathml(mathml)
                .semanticAst(ast)
                .build();
    }

    public String toLatex(String expr) {
        if (expr == null) return "";
        String res = expr.trim();
        res = res.replace("*", " \\cdot ");
        res = res.replace("/", " / ");
        return res;
    }

    public String toMathML(String expr) {
        if (expr == null) return "";
        return "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mrow><mtext>" + expr.replace("<", "&lt;").replace(">", "&gt;") + "</mtext></mrow></math>";
    }

    public ObjectNode buildSimpleAst(String expr) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "expression");
        root.put("raw", expr);
        return root;
    }
}
