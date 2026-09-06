package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.AnimationAction;
import ai.anvaya.prajna.ir.AnimationEvent;
import ai.anvaya.prajna.ir.AnimationIR;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepType;

import java.util.ArrayList;
import java.util.List;

public class AnimationCompiler {

    public AnimationIR compile(List<ReasoningStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return null;
        }

        List<AnimationEvent> timeline = new ArrayList<>();
        int currentTime = 0;

        // Step 0: SHOW initial problem / equation
        timeline.add(AnimationEvent.builder()
                .at(currentTime)
                .action(AnimationAction.SHOW)
                .target("problem-view")
                .build());

        currentTime += 1000;

        for (ReasoningStep step : steps) {
            String targetId = step.getId() != null ? step.getId() : "step";

            if (step.getType() == StepType.TRANSFORM || step.getType() == StepType.CALCULATE) {
                // Highlight step
                timeline.add(AnimationEvent.builder()
                        .at(currentTime)
                        .action(AnimationAction.HIGHLIGHT)
                        .target(targetId)
                        .build());
                currentTime += 1000;

                // Transform action
                String opName = step.getOperation() != null ? step.getOperation().getName() : "TRANSFORM";
                Object opVal = step.getOperation() != null ? step.getOperation().getValue() : null;

                timeline.add(AnimationEvent.builder()
                        .at(currentTime)
                        .action(AnimationAction.TRANSFORM)
                        .target(targetId)
                        .operation(opName)
                        .value(opVal)
                        .build());
                currentTime += 1200;

                // Reveal output
                timeline.add(AnimationEvent.builder()
                        .at(currentTime)
                        .action(AnimationAction.REVEAL)
                        .target(targetId + "-result")
                        .build());
                currentTime += 800;
            } else {
                timeline.add(AnimationEvent.builder()
                        .at(currentTime)
                        .action(AnimationAction.SHOW)
                        .target(targetId)
                        .build());
                currentTime += 1000;
            }
        }

        return AnimationIR.builder()
                .durationMs(currentTime)
                .timeline(timeline)
                .build();
    }
}
