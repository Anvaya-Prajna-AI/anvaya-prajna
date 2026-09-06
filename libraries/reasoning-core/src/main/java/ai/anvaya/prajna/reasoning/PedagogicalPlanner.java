package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.ir.StudentLevel;

import java.util.ArrayList;
import java.util.List;

public class PedagogicalPlanner {

    public List<ReasoningStep> planSteps(List<ReasoningStep> steps, ExplanationPolicy policy) {
        if (steps == null || steps.isEmpty()) {
            return List.of();
        }

        ExplanationPolicy effectivePolicy = policy != null ? policy : ExplanationPolicy.builder().build();
        StudentLevel level = effectivePolicy.getLevel() != null ? effectivePolicy.getLevel() : StudentLevel.INTERMEDIATE;

        List<ReasoningStep> planned = new ArrayList<>();
        int seq = 1;

        for (ReasoningStep step : steps) {
            // Check formula suppression
            if (!Boolean.TRUE.equals(effectivePolicy.getShowFormula()) && step.getType() == StepType.APPLY_RULE) {
                continue;
            }

            // Check calculation suppression
            if (!Boolean.TRUE.equals(effectivePolicy.getShowEveryCalculation()) && step.getType() == StepType.CALCULATE && level == StudentLevel.ADVANCED) {
                continue;
            }

            // Check verification suppression
            if (!Boolean.TRUE.equals(effectivePolicy.getShowVerification()) && step.getType() == StepType.VERIFY) {
                continue;
            }

            // Create step with updated sequence
            ReasoningStep adjusted = ReasoningStep.builder()
                    .id(step.getId())
                    .sequence(seq++)
                    .type(step.getType())
                    .inputs(step.getInputs())
                    .operation(step.getOperation())
                    .before(step.getBefore())
                    .after(step.getAfter())
                    .outputs(step.getOutputs())
                    .justification(Boolean.TRUE.equals(effectivePolicy.getShowWhy()) ? step.getJustification() : null)
                    .representations(step.getRepresentations() != null ? new ArrayList<>(step.getRepresentations()) : new ArrayList<>())
                    .math(step.getMath())
                    .verification(step.getVerification())
                    .misconception(Boolean.TRUE.equals(effectivePolicy.getShowMisconceptions()) ? step.getMisconception() : null)
                    .build();

            planned.add(adjusted);
        }

        return planned;
    }
}
