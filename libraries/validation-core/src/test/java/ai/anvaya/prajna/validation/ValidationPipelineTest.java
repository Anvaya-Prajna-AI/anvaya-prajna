package ai.anvaya.prajna.validation;

import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepOperation;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.ir.VerificationResult;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationPipelineTest {

    private final ValidationPipeline pipeline = new ValidationPipeline();
    private final JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator();

    @Test
    void shouldPassValidProposal() {
        Question question = Question.builder()
                .questionId("q1")
                .statement("Solve 3x + 5 = 20")
                .authoritativeAnswer("5")
                .build();

        ReasoningStep s1 = ReasoningStep.builder()
                .id("s1")
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder().name("SUBTRACT").target("both-sides").value(5).build())
                .before("3x + 5 = 20")
                .after("3x = 15")
                .build();

        ReasoningStep s2 = ReasoningStep.builder()
                .id("s2")
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder().name("DIVIDE").target("both-sides").value(3).build())
                .before("3x = 15")
                .after("x = 5")
                .build();

        ReasoningProposal proposal = ReasoningProposal.builder()
                .questionId("q1")
                .steps(List.of(s1, s2))
                .conclusion("x = 5")
                .verification(VerificationResult.builder()
                        .type("SUBSTITUTION")
                        .expression("3 * 5 + 5 = 20")
                        .expected(true)
                        .build())
                .build();

        ValidationResult result = pipeline.validate(question, proposal);
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.PASSED);
        assertThat(result.getViolations()).isEmpty();
    }

    @Test
    void shouldRejectUnsafeScriptContent() {
        Question question = Question.builder().questionId("q1").build();
        ReasoningProposal proposal = ReasoningProposal.builder()
                .problemSummary("<script>alert('xss')</script>")
                .steps(List.of())
                .build();

        ValidationResult result = pipeline.validate(question, proposal);
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.FAILED);
        assertThat(result.getViolations()).anyMatch(v -> v.getCode().equals("SECURITY_VIOLATION"));
    }

    @Test
    void shouldValidateJsonAgainstSchema() {
        String validJson = "{\"schemaVersion\":\"1.0\",\"explanationId\":\"exp-1\",\"questionId\":\"q-1\",\"problem\":{\"statement\":\"test\"},\"steps\":[]}";
        ValidationResult result = jsonSchemaValidator.validateJson(validJson);
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.PASSED);
    }
}
