package ai.anvaya.prajna.ir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExplanationIRTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldSerializeAndDeserializeExplanationIR() throws Exception {
        ReasoningStep step1 = ReasoningStep.builder()
                .id("s1")
                .sequence(1)
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder().name("SUBTRACT").target("both-sides").value(5).build())
                .before("3x + 5 = 20")
                .after("3x = 15")
                .justification(StepJustification.builder().text("Subtract 5 from both sides").build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH, RepresentationType.ANIMATION))
                .build();

        ExplanationIR ir = ExplanationIR.builder()
                .schemaVersion("1.0")
                .explanationId("exp-001")
                .questionId("q-001")
                .status(ExplanationStatus.APPROVED)
                .problem(Problem.builder().statement("If 3x + 5 = 20, find x.").build())
                .concepts(List.of("linear-equation", "inverse-operation"))
                .steps(List.of(step1))
                .verification(VerificationResult.builder()
                        .type("SUBSTITUTION")
                        .expression("3 * 5 + 5 = 20")
                        .expected(true)
                        .actual(true)
                        .passed(true)
                        .build())
                .hints(List.of(Hint.builder().id("h1").level(1).text("Subtract 5 from both sides").build()))
                .build();

        String json = objectMapper.writeValueAsString(ir);
        assertThat(json).contains("exp-001");
        assertThat(json).contains("linear-equation");

        ExplanationIR deserialized = objectMapper.readValue(json, ExplanationIR.class);
        assertThat(deserialized.getExplanationId()).isEqualTo("exp-001");
        assertThat(deserialized.getSteps()).hasSize(1);
        assertThat(deserialized.getSteps().get(0).getType()).isEqualTo(StepType.TRANSFORM);
        assertThat(deserialized.getVerification().getPassed()).isTrue();
    }
}
