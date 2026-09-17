package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.RepresentationType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RepresentationPlanner {

    public List<ReasoningStep> assignRepresentations(List<ReasoningStep> steps, String domain, ExplanationPolicy policy) {
        if (steps == null) {
            return List.of();
        }

        boolean animationEnabled = policy != null && !"NONE".equalsIgnoreCase(policy.getAnimation());

        List<ReasoningStep> result = new ArrayList<>();
        for (ReasoningStep step : steps) {
            Set<RepresentationType> reps = new LinkedHashSet<>();
            if (step.getRepresentations() != null) {
                reps.addAll(step.getRepresentations());
            }

            // Always add TEXT representation
            reps.add(RepresentationType.TEXT);

            // Add MATH representation if formula/before/after contains math symbols
            if (step.getMath() != null || (step.getBefore() != null && hasMath(step.getBefore())) || (step.getAfter() != null && hasMath(step.getAfter()))) {
                reps.add(RepresentationType.MATH);
            }

            // Add ANIMATION if enabled and step is a transformation
            if (animationEnabled && step.getType() != null) {
                switch (step.getType()) {
                    case TRANSFORM:
                    case CALCULATE:
                    case SUBSTITUTE:
                        reps.add(RepresentationType.ANIMATION);
                        reps.add(RepresentationType.HIGHLIGHT);
                        break;
                    default:
                        break;
                }
            }

            if ("LOGICAL_REASONING".equalsIgnoreCase(domain) || "SYLLOGISM".equalsIgnoreCase(domain)) {
                reps.add(RepresentationType.REASONING_GRAPH);
            }

            ReasoningStep updated = ReasoningStep.builder()
                    .id(step.getId())
                    .sequence(step.getSequence())
                    .type(step.getType())
                    .inputs(step.getInputs())
                    .operation(step.getOperation())
                    .before(step.getBefore())
                    .after(step.getAfter())
                    .outputs(step.getOutputs())
                    .justification(step.getJustification())
                    .representations(new ArrayList<>(reps))
                    .math(step.getMath())
                    .verification(step.getVerification())
                    .misconception(step.getMisconception())
                    .build();

            result.add(updated);
        }

        return result;
    }

    private boolean hasMath(String s) {
        return s.contains("=") || s.contains("+") || s.contains("-") || s.contains("*") || s.contains("/") || s.contains("^");
    }
}
