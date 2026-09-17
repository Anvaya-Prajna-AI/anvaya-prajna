package ai.anvaya.prajna.math;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionEvaluator {

    private static final double EPSILON = 1e-6;

    /**
     * Normalizes math strings:
     * - Replaces LaTeX symbols: \times -> *, \div -> /, \cdot -> *
     * - Handles implicit multiplication like 3x -> 3*x, 3(5) -> 3*(5)
     * - Removes dollar signs and spaces
     */
    public String normalizeExpression(String raw) {
        if (raw == null) {
            return "";
        }
        String expr = raw.trim();
        expr = expr.replace("$", "")
                .replace("\\times", "*")
                .replace("×", "*")
                .replace("\\div", "/")
                .replace("÷", "/")
                .replace("\\cdot", "*")
                .replace("·", "*")
                .replace("\\frac{", "(") // basic replacement
                .replace("}{", ")/(")
                .replace("}", ")");

        // Replace patterns like digit followed by variable: e.g. 3x -> 3*x, 3(x) -> 3*(x)
        expr = expr.replaceAll("(\\d+)([a-zA-Z])", "$1*$2");
        expr = expr.replaceAll("(\\d+)\\(", "$1*(");
        expr = expr.replaceAll("\\)(\\d+)", ")*$1");
        expr = expr.replaceAll("\\)\\s*\\(", ")*(");
        expr = expr.replaceAll("([a-zA-Z])\\(", "$1*(");

        return expr.trim();
    }

    /**
     * Evaluates a numeric expression string.
     */
    public double evaluate(String expression) {
        return evaluate(expression, Map.of());
    }

    /**
     * Evaluates an expression with a map of variable bindings.
     */
    public double evaluate(String expression, Map<String, Double> variables) {
        String normalized = normalizeExpression(expression);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Expression is empty");
        }

        ExpressionBuilder builder = new ExpressionBuilder(normalized);
        if (variables != null && !variables.isEmpty()) {
            builder.variables(variables.keySet());
        }

        Expression exp = builder.build();
        if (variables != null) {
            for (Map.Entry<String, Double> entry : variables.entrySet()) {
                exp.setVariable(entry.getKey(), entry.getValue());
            }
        }
        return exp.evaluate();
    }

    /**
     * Checks if two expressions are numerically equivalent across sample test points.
     */
    public boolean checkEquality(String expr1, String expr2, Set<String> variables) {
        if (expr1 == null || expr2 == null) {
            return false;
        }

        if (variables == null || variables.isEmpty()) {
            try {
                double val1 = evaluate(expr1);
                double val2 = evaluate(expr2);
                return Math.abs(val1 - val2) < EPSILON;
            } catch (Exception e) {
                return false;
            }
        }

        // Test with multiple deterministic sample points: 0.5, 1.0, 2.0, 5.0, 10.0
        double[] testPoints = {0.5, 1.0, 2.0, 5.0, 10.0};
        for (double pt : testPoints) {
            Map<String, Double> varMap = new HashMap<>();
            for (String var : variables) {
                varMap.put(var, pt);
            }
            try {
                double val1 = evaluate(expr1, varMap);
                double val2 = evaluate(expr2, varMap);
                if (Double.isNaN(val1) || Double.isNaN(val2) || Math.abs(val1 - val2) >= EPSILON) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Evaluates an equality string like "3 * 5 + 5 = 20" or "LHS = RHS".
     */
    public boolean evaluateEquality(String equation, Map<String, Double> variables) {
        if (equation == null || !equation.contains("=")) {
            return false;
        }
        String[] sides = equation.split("=", 2);
        String lhs = sides[0].trim();
        String rhs = sides[1].trim();

        try {
            double lhsVal = evaluate(lhs, variables != null ? variables : Map.of());
            double rhsVal = evaluate(rhs, variables != null ? variables : Map.of());
            return Math.abs(lhsVal - rhsVal) < EPSILON;
        } catch (Exception e) {
            return false;
        }
    }
}
