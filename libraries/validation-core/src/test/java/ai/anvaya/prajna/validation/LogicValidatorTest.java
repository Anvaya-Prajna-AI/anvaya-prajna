package ai.anvaya.prajna.validation;

import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogicValidatorTest {

    private final LogicValidator validator = new LogicValidator();

    @Test
    void shouldPassValidSequence() {
        ReasoningProposal proposal = ReasoningProposal.builder()
                .facts(List.of("A = B", "B = C"))
                .steps(List.of(
                        ReasoningStep.builder().id("s1").inputs(List.of("f1", "f2")).after("A = C").build(),
                        ReasoningStep.builder().id("s2").inputs(List.of("s1")).after("Done").build()
                ))
                .build();

        ValidationResult result = validator.validate(Question.builder().build(), proposal);
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.PASSED);
    }

    @Test
    void shouldDetectDuplicateStepIds() {
        ReasoningProposal proposal = ReasoningProposal.builder()
                .steps(List.of(
                        ReasoningStep.builder().id("s1").after("A").build(),
                        ReasoningStep.builder().id("s1").after("B").build()
                ))
                .build();

        ValidationResult result = validator.validate(Question.builder().build(), proposal);
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.FAILED);
        assertThat(result.getViolations()).anyMatch(v -> "DUPLICATE_STEP_ID".equals(v.getCode()));
    }
}
