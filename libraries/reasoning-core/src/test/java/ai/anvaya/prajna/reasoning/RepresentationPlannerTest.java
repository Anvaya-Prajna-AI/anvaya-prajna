package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.RepresentationType;
import ai.anvaya.prajna.ir.StepType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepresentationPlannerTest {

    private final RepresentationPlanner planner = new RepresentationPlanner();

    @Test
    void shouldHandleNullSteps() {
        assertThat(planner.assignRepresentations(null, "ALGEBRA", null)).isEmpty();
    }

    @Test
    void shouldAssignTextMathAndAnimationRepresentations() {
        ReasoningStep step = ReasoningStep.builder()
                .id("s1")
                .sequence(1)
                .type(StepType.TRANSFORM)
                .before("2x + 4 = 10")
                .after("2x = 6")
                .build();

        ExplanationPolicy policy = ExplanationPolicy.builder().animation("SMOOTH").build();
        List<ReasoningStep> result = planner.assignRepresentations(List.of(step), "ALGEBRA", policy);

        assertThat(result).hasSize(1);
        List<RepresentationType> reps = result.get(0).getRepresentations();
        assertThat(reps).contains(RepresentationType.TEXT, RepresentationType.MATH, RepresentationType.ANIMATION);
    }

    @Test
    void shouldAssignReasoningGraphForSyllogismDomain() {
        ReasoningStep step = ReasoningStep.builder().id("s1").type(StepType.INFER).after("Conclusion follows").build();
        List<ReasoningStep> result = planner.assignRepresentations(List.of(step), "SYLLOGISM", null);

        assertThat(result.get(0).getRepresentations()).contains(RepresentationType.REASONING_GRAPH);
    }
}
