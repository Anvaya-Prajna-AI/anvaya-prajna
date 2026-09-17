package ai.anvaya.prajna.application;

import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompilationServiceTest {

    private final CompilationService service = new CompilationService();

    @Test
    void shouldCompileQuestionAndProposalIntoExplanationIR() {
        Question question = Question.builder()
                .questionId("q-comp-1")
                .statement("What is 5 + 7?")
                .authoritativeAnswer("12")
                .build();

        ReasoningProposal proposal = ReasoningProposal.builder()
                .questionId("q-comp-1")
                .problemSummary("Calculate 5 + 7")
                .steps(List.of(
                        ReasoningStep.builder().id("s1").before("5 + 7").after("12").build()
                ))
                .conclusion("12")
                .build();

        ExplanationIR ir = service.compileExplanation(question, proposal, ExplanationPolicy.builder().build());

        assertThat(ir).isNotNull();
        assertThat(ir.getQuestionId()).isEqualTo("q-comp-1");
        assertThat(ir.getSteps()).hasSize(1);
    }
}
