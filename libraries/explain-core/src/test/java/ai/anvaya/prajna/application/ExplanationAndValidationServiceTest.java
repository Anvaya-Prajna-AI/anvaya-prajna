package ai.anvaya.prajna.application;

import ai.anvaya.prajna.domain.entity.ExplanationEntity;
import ai.anvaya.prajna.domain.entity.ExplanationValidationEntity;
import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.Hint;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.StepJustification;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import ai.anvaya.prajna.repository.ExplanationRepository;
import ai.anvaya.prajna.repository.ExplanationValidationRepository;
import ai.anvaya.prajna.validation.ValidationResult;
import ai.anvaya.prajna.validation.ValidationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ExplanationAndValidationServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testHintServiceRetrieval() throws Exception {
        ExplanationRepository repo = Mockito.mock(ExplanationRepository.class);
        ExplanationIR ir = ExplanationIR.builder()
                .questionId("q-test")
                .hints(List.of(
                        Hint.builder().id("h1").level(1).text("Nudge 1").build(),
                        Hint.builder().id("h2").level(2).text("Strategy 2").build()
                ))
                .build();

        ExplanationEntity entity = ExplanationEntity.builder()
                .id(UUID.randomUUID())
                .questionId("q-test")
                .contentJson(objectMapper.writeValueAsString(ir))
                .build();

        when(repo.findFirstByQuestionIdOrderByVersionDesc("q-test")).thenReturn(Optional.of(entity));

        HintService hintService = new HintService(repo, null, objectMapper);
        Optional<Hint> hint = hintService.getHint("q-test", 1);

        assertThat(hint).isPresent();
        assertThat(hint.get().getText()).isEqualTo("Nudge 1");
    }

    @Test
    void testWhyServiceRetrieval() throws Exception {
        ExplanationRepository repo = Mockito.mock(ExplanationRepository.class);
        ExplanationIR ir = ExplanationIR.builder()
                .questionId("q-test")
                .steps(List.of(
                        ai.anvaya.prajna.ir.ReasoningStep.builder()
                                .id("s1")
                                .justification(StepJustification.builder().text("Because arithmetic").build())
                                .build()
                ))
                .build();

        ExplanationEntity entity = ExplanationEntity.builder()
                .id(UUID.randomUUID())
                .questionId("q-test")
                .contentJson(objectMapper.writeValueAsString(ir))
                .build();

        when(repo.findFirstByQuestionIdOrderByVersionDesc("q-test")).thenReturn(Optional.of(entity));

        WhyService whyService = new WhyService(repo, objectMapper);
        Optional<StepJustification> why = whyService.getWhyForStep("q-test", "s1");

        assertThat(why).isPresent();
        assertThat(why.get().getText()).isEqualTo("Because arithmetic");
    }

    @Test
    void testValidationServiceExecution() {
        ExplanationValidationRepository valRepo = Mockito.mock(ExplanationValidationRepository.class);
        ValidationService service = new ValidationService(valRepo, objectMapper);

        Question q = Question.builder().statement("2 + 2").build();
        ReasoningProposal proposal = ReasoningProposal.builder().conclusion("4").build();

        ValidationResult result = service.validateProposal(q, proposal);
        assertThat(result.getStatus()).isEqualTo(ValidationStatus.PASSED);

        service.recordValidationResult(UUID.randomUUID(), result);
        Mockito.verify(valRepo).save(any(ExplanationValidationEntity.class));
    }
}
