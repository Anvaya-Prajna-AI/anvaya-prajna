package ai.anvaya.prajna.validation;

import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SafetyValidatorTest {

    private final SafetyValidator validator = new SafetyValidator();

    @Test
    void shouldPassCleanProposal() {
        ReasoningProposal proposal = ReasoningProposal.builder()
                .problemSummary("Clean math problem")
                .conclusion("x = 5")
                .build();

        ValidationResult result = validator.validate(Question.builder().build(), proposal);
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.PASSED);
    }

    @Test
    void shouldDetectXssOrJavascriptInjection() {
        ReasoningProposal proposal = ReasoningProposal.builder()
                .steps(List.of(
                        ReasoningStep.builder().id("s1").after("<script>alert(1)</script>").build()
                ))
                .build();

        ValidationResult result = validator.validate(Question.builder().build(), proposal);
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.FAILED);
        assertThat(result.getViolations()).anyMatch(v -> "SECURITY_VIOLATION".equals(v.getCode()));
    }
}
