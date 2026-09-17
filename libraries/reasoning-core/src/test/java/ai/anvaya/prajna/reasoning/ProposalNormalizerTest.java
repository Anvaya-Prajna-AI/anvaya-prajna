package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProposalNormalizerTest {

    private final ProposalNormalizer normalizer = new ProposalNormalizer();

    @Test
    void shouldHandleNullProposal() {
        ReasoningProposal normalized = normalizer.normalize(null);
        assertThat(normalized).isNotNull();
        assertThat(normalized.getSteps()).isEmpty();
    }

    @Test
    void shouldNormalizeStepsWithDefaultIdAndJustification() {
        ReasoningStep step = ReasoningStep.builder()
                .before("2x = 8")
                .after("x = 4")
                .build();

        ReasoningProposal proposal = ReasoningProposal.builder()
                .questionId("q100")
                .problemSummary(" Solve for x ")
                .steps(List.of(step))
                .build();

        ReasoningProposal result = normalizer.normalize(proposal);
        assertThat(result.getProblemSummary()).isEqualTo("Solve for x");
        assertThat(result.getSteps()).hasSize(1);

        ReasoningStep normalizedStep = result.getSteps().get(0);
        assertThat(normalizedStep.getId()).isEqualTo("s1");
        assertThat(normalizedStep.getSequence()).isEqualTo(1);
        assertThat(normalizedStep.getType()).isEqualTo(StepType.INFER);
        assertThat(normalizedStep.getJustification()).isNotNull();
        assertThat(normalizedStep.getMath()).isNotNull();
    }
}
