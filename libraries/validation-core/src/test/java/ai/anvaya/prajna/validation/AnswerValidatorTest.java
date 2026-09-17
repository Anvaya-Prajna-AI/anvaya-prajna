package ai.anvaya.prajna.validation;

import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerValidatorTest {

    private final AnswerValidator validator = new AnswerValidator();

    @Test
    void shouldPassWhenNoAuthoritativeAnswerGiven() {
        Question q = Question.builder().questionId("q1").build();
        ValidationResult result = validator.validate(q, null);
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.PASSED);
    }

    @Test
    void shouldPassWhenConclusionMatchesAuthoritativeAnswer() {
        Question q = Question.builder().questionId("q1").authoritativeAnswer("5").build();
        ReasoningProposal proposal = ReasoningProposal.builder()
                .conclusion("The answer is 5")
                .build();

        ValidationResult result = validator.validate(q, proposal);
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.PASSED);
        assertThat(result.getScore()).isEqualTo(1.0);
    }

    @Test
    void shouldFailWhenConclusionMismatchesAuthoritativeAnswer() {
        Question q = Question.builder().questionId("q1").authoritativeAnswer("5").build();
        ReasoningProposal proposal = ReasoningProposal.builder()
                .conclusion("The answer is 12")
                .build();

        ValidationResult result = validator.validate(q, proposal);
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.FAILED);
        assertThat(result.getViolations()).isNotEmpty();
    }
}
