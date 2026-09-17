package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.AnimationAction;
import ai.anvaya.prajna.ir.AnimationIR;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepOperation;
import ai.anvaya.prajna.ir.StepType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnimationCompilerTest {

    private final AnimationCompiler compiler = new AnimationCompiler();

    @Test
    void shouldReturnNullForEmptySteps() {
        assertThat(compiler.compile(null)).isNull();
        assertThat(compiler.compile(List.of())).isNull();
    }

    @Test
    void shouldCompileTimelineForTransformStep() {
        ReasoningStep step = ReasoningStep.builder()
                .id("s1")
                .sequence(1)
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder().name("SUBTRACT").value(5).build())
                .before("3x + 5 = 20")
                .after("3x = 15")
                .build();

        AnimationIR ir = compiler.compile(List.of(step));
        assertThat(ir).isNotNull();
        assertThat(ir.getDurationMs()).isGreaterThan(0);
        assertThat(ir.getTimeline()).isNotEmpty();
        assertThat(ir.getTimeline()).anyMatch(event -> event.getAction() == AnimationAction.TRANSFORM);
        assertThat(ir.getTimeline()).anyMatch(event -> event.getAction() == AnimationAction.REVEAL);
    }
}
