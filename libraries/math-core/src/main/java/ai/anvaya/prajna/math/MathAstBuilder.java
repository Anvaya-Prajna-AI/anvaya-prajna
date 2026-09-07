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
        // Handle simple fractions: e.g. "a / b" -> "\frac{a}{b}"
        if (res.matches(".+\\s*/\\s*.+")) {
            String[] parts = res.split("\\s*/\\s*", 2);
            res = "\\frac{" + parts[0].trim() + "}{" + parts[1].trim() + "}";
        }
        // Handle exponents: e.g. "x^2" -> "x^{2}"
        res = res.replaceAll("\\^(\\d+)", "^{$1}");
        return res;
    }

    public String toMathML(String expr) {
        if (expr == null) return "";
        String safe = expr.replace("<", "&lt;").replace(">", "&gt;");
        if (safe.contains("=")) {
            String[] sides = safe.split("=", 2);
            return "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mrow><mtext>" 
                    + sides[0].trim() + "</mtext><mo>=</mo><mtext>" + sides[1].trim() + "</mtext></mrow></math>";
        }
        return "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mrow><mtext>" + safe + "</mtext></mrow></math>";
    }

    public ObjectNode buildSimpleAst(String expr) {
        ObjectNode root = objectMapper.createObjectNode();
        if (expr == null) {
            root.put("type", "null");
            return root;
        }

        String trimmed = expr.trim();
        if (trimmed.contains("=")) {
            String[] sides = trimmed.split("=", 2);
            root.put("type", "equation");
            root.put("operator", "=");
            ObjectNode left = objectMapper.createObjectNode();
            left.put("type", "expression");
            left.put("raw", sides[0].trim());
            ObjectNode right = objectMapper.createObjectNode();
            right.put("type", "expression");
            right.put("raw", sides[1].trim());
            root.set("left", left);
            root.set("right", right);
            return root;
        }

        root.put("type", "expression");
        root.put("raw", trimmed);
        return root;
    }
}
