package ai.anvaya.prajna.domain.entity;

import ai.anvaya.prajna.security.EngineCallerPrincipal;
import ai.anvaya.prajna.security.EngineSecurityContextHolder;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EntitiesAndSecurityContextTest {

    @Test
    void shouldConstructAndValidateEntities() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        ExplanationEntity entity = ExplanationEntity.builder()
                .id(id)
                .questionId("q-test")
                .version(1)
                .status("APPROVED")
                .reviewedBy("teacher-1")
                .reviewedAt(now)
                .build();

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getReviewedBy()).isEqualTo("teacher-1");

        ExplanationFeedbackEntity feedback = ExplanationFeedbackEntity.builder()
                .id(UUID.randomUUID())
                .explanationId(id)
                .userId("student-1")
                .feedbackType("HELPFUL")
                .comment("Great!")
                .build();

        assertThat(feedback.getFeedbackType()).isEqualTo("HELPFUL");

        ExplanationStepEntity stepEntity = ExplanationStepEntity.builder()
                .id(UUID.randomUUID())
                .explanationId(id)
                .sequence(1)
                .stepType("TRANSFORM")
                .validationStatus("VALID")
                .build();

        assertThat(stepEntity.getSequence()).isEqualTo(1);

        ExplanationTemplateEntity template = ExplanationTemplateEntity.builder()
                .id(UUID.randomUUID())
                .domain("ALGEBRA")
                .templateType("LinearEquation")
                .contentJson("{}")
                .version(1)
                .status("ACTIVE")
                .build();

        assertThat(template.getDomain()).isEqualTo("ALGEBRA");

        ExplanationValidationEntity valEntity = ExplanationValidationEntity.builder()
                .id(UUID.randomUUID())
                .explanationId(id)
                .status("PASSED")
                .score(1.0)
                .build();

        assertThat(valEntity.getStatus()).isEqualTo("PASSED");
    }

    @Test
    void shouldManageEngineSecurityContextHolder() {
        EngineCallerPrincipal principal = new EngineCallerPrincipal("user-99", "tenant-1", Set.of("ROLE_ADMIN"), true);
        EngineSecurityContextHolder.setContext(principal);

        assertThat(EngineSecurityContextHolder.getContext().getUserId()).isEqualTo("user-99");
        assertThat(EngineSecurityContextHolder.getContext().hasRole("ADMIN")).isTrue();

        EngineSecurityContextHolder.clearContext();
        assertThat(EngineSecurityContextHolder.getContext().getUserId()).isEqualTo("anonymous");
    }
}
