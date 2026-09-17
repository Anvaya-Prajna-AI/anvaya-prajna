package ai.anvaya.prajna.security;

import ai.anvaya.prajna.ir.Question;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultEngineSecurityAuthorizerTest {

    private final DefaultEngineSecurityAuthorizer authorizer = new DefaultEngineSecurityAuthorizer();

    @Test
    void shouldPermitGenerateForAnyone() {
        EngineSecurityContext ctx = new EngineCallerPrincipal("u1", "t1", Set.of("ROLE_STUDENT"), true);
        assertThatCode(() -> authorizer.authorizeGenerate(Question.builder().build(), null, ctx))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldDenyReviewForStudent() {
        EngineSecurityContext ctx = new EngineCallerPrincipal("u1", "t1", Set.of("ROLE_STUDENT"), true);
        assertThatThrownBy(() -> authorizer.authorizeReview(UUID.randomUUID(), true, ctx))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("ASI03");
    }

    @Test
    void shouldAllowReviewForEducatorAndAdmin() {
        EngineSecurityContext educatorCtx = new EngineCallerPrincipal("u1", "t1", Set.of("ROLE_EDUCATOR"), true);
        EngineSecurityContext adminCtx = new EngineCallerPrincipal("u2", "t1", Set.of("ROLE_ADMIN"), true);

        assertThatCode(() -> authorizer.authorizeReview(UUID.randomUUID(), true, educatorCtx))
                .doesNotThrowAnyException();
        assertThatCode(() -> authorizer.authorizeReview(UUID.randomUUID(), true, adminCtx))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldDenyPublishForNonAdmin() {
        EngineSecurityContext educatorCtx = new EngineCallerPrincipal("u1", "t1", Set.of("ROLE_EDUCATOR"), true);
        assertThatThrownBy(() -> authorizer.authorizePublish(UUID.randomUUID(), educatorCtx))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("ROLE_ADMIN");
    }

    @Test
    void shouldAllowPublishForAdmin() {
        EngineSecurityContext adminCtx = new EngineCallerPrincipal("u2", "t1", Set.of("ROLE_ADMIN"), true);
        assertThatCode(() -> authorizer.authorizePublish(UUID.randomUUID(), adminCtx))
                .doesNotThrowAnyException();
    }
}
