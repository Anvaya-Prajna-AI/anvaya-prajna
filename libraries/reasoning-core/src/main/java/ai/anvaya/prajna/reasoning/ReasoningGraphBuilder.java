package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.ReasoningGraphEdge;
import ai.anvaya.prajna.ir.ReasoningGraphIR;
import ai.anvaya.prajna.ir.ReasoningGraphNode;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepType;

import java.util.ArrayList;
import java.util.List;

public class ReasoningGraphBuilder {

    public ReasoningGraphIR buildGraph(List<String> facts, List<ReasoningStep> steps, String conclusion) {
        List<ReasoningGraphNode> nodes = new ArrayList<>();
        List<ReasoningGraphEdge> edges = new ArrayList<>();

        // Add facts as nodes
        if (facts != null) {
            int factIdx = 1;
            for (String fact : facts) {
                String factId = "f" + (factIdx++);
                nodes.add(ReasoningGraphNode.builder()
                        .id(factId)
                        .type("FACT")
                        .content(fact)
                        .source("GIVEN")
                        .confidence(1.0)
                        .validationStatus("VALID")
                        .build());
            }
        }

        // Add steps as nodes
        String previousStepId = null;
        if (steps != null) {
            for (ReasoningStep step : steps) {
                String nodeType = mapStepTypeToNodeType(step.getType());
                String content = step.getAfter() != null ? step.getAfter() : (step.getJustification() != null ? step.getJustification().getText() : step.getId());

                nodes.add(ReasoningGraphNode.builder()
                        .id(step.getId())
                        .type(nodeType)
                        .content(content)
                        .source("AI")
                        .confidence(0.95)
                        .validationStatus("VALID")
                        .build());

                // Edge from previous step
                if (previousStepId != null) {
                    edges.add(ReasoningGraphEdge.builder()
                            .from(previousStepId)
                            .to(step.getId())
                            .relationship("DEPENDS_ON")
                            .justification(step.getJustification() != null ? step.getJustification().getText() : null)
                            .build());
                } else if (!nodes.isEmpty() && nodes.get(0).getType().equals("FACT")) {
                    edges.add(ReasoningGraphEdge.builder()
                            .from(nodes.get(0).getId())
                            .to(step.getId())
                            .relationship("DERIVED_FROM")
                            .build());
                }

                previousStepId = step.getId();
            }
        }

        // Add conclusion node if present
        if (conclusion != null && !conclusion.isBlank()) {
            String concId = "conclusion";
            nodes.add(ReasoningGraphNode.builder()
                    .id(concId)
                    .type("CONCLUSION")
                    .content(conclusion)
                    .source("AI")
                    .confidence(1.0)
                    .validationStatus("VALID")
                    .build());

            if (previousStepId != null) {
                edges.add(ReasoningGraphEdge.builder()
                        .from(previousStepId)
                        .to(concId)
                        .relationship("SUPPORTS")
                        .build());
            }
        }

        return ReasoningGraphIR.builder()
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    private String mapStepTypeToNodeType(StepType type) {
        if (type == null) return "RESULT";
        switch (type) {
            case GIVEN:
                return "FACT";
            case APPLY_RULE:
                return "RULE";
            case CONCLUDE:
                return "CONCLUSION";
            case VERIFY:
                return "VERIFICATION";
            default:
                return "RESULT";
        }
    }
}
