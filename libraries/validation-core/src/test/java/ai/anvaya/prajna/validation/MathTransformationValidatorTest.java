package ai.anvaya.prajna.validation;

import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepOperation;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MathTransformationValidatorTest {

    private final MathTransformationValidator validator = new MathTransformationValidator();

    @Test
    void shouldPassValidTransformationStep() {
        ReasoningStep step = ReasoningStep.builder()
                .id("s1")
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder().name("SUBTRACT").target("both-sides").value(5).build())
                .before("3*x + 5 = 20")
                .after("3*x = 15")
                .build();

        ReasoningProposal proposal = ReasoningProposal.builder().steps(List.of(step)).build();
        ValidationResult result = validator.validate(Question.builder().build(), proposal);
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.PASSED);
    }
}
