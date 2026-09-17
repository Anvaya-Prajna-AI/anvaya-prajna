package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.ReasoningGraphIR;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReasoningGraphBuilderTest {

    private final ReasoningGraphBuilder builder = new ReasoningGraphBuilder();

    @Test
    void shouldBuildGraphFromFactsStepsAndConclusion() {
        List<String> facts = List.of("All mammals are warm-blooded", "Whales are mammals");
        List<ReasoningStep> steps = List.of(
                ReasoningStep.builder().id("s1").type(StepType.APPLY_RULE).after("Apply universal instantiation").build(),
                ReasoningStep.builder().id("s2").type(StepType.CONCLUDE).after("Whales are warm-blooded").build()
        );

        ReasoningGraphIR graph = builder.buildGraph(facts, steps, "Whales are warm-blooded");
        assertThat(graph).isNotNull();
        assertThat(graph.getNodes()).hasSize(5); // 2 facts + 2 steps + 1 conclusion
        assertThat(graph.getEdges()).isNotEmpty();
        assertThat(graph.getNodes()).anyMatch(n -> "FACT".equals(n.getType()));
        assertThat(graph.getNodes()).anyMatch(n -> "CONCLUSION".equals(n.getType()));
    }
}
