package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepOperation;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.ir.StudentLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExplanationCompilerTest {

    private final ExplanationCompiler compiler = new ExplanationCompiler();

    @Test
    void shouldCompileProposalIntoExplanationIR() {
        Question question = Question.builder()
                .questionId("q-algebra-1")
                .statement("If 3x + 5 = 20, find x.")
                .domain("ALGEBRA")
                .authoritativeAnswer("x = 5")
                .build();

        ReasoningStep s1 = ReasoningStep.builder()
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder().name("SUBTRACT").target("both-sides").value(5).build())
                .before("3x + 5 = 20")
                .after("3x = 15")
                .build();

        ReasoningStep s2 = ReasoningStep.builder()
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder().name("DIVIDE").target("both-sides").value(3).build())
                .before("3x = 15")
                .after("x = 5")
                .build();

        ReasoningProposal proposal = ReasoningProposal.builder()
                .questionId("q-algebra-1")
                .concepts(List.of("linear-equation", "inverse-operation"))
                .steps(List.of(s1, s2))
                .conclusion("x = 5")
                .build();

        ExplanationPolicy policy = ExplanationPolicy.builder()
                .level(StudentLevel.INTERMEDIATE)
                .animation("LIGHT")
                .build();

        ExplanationIR ir = compiler.compile(question, proposal, policy);

        assertThat(ir).isNotNull();
        assertThat(ir.getExplanationId()).startsWith("exp-");
        assertThat(ir.getSteps()).hasSize(2);
        assertThat(ir.getSteps().get(0).getId()).isEqualTo("s1");
        assertThat(ir.getSteps().get(1).getId()).isEqualTo("s2");
        assertThat(ir.getReasoningGraph()).isNotNull();
        assertThat(ir.getReasoningGraph().getNodes()).isNotEmpty();
        assertThat(ir.getAnimation()).isNotNull();
        assertThat(ir.getAnimation().getTimeline()).isNotEmpty();
    }
}
