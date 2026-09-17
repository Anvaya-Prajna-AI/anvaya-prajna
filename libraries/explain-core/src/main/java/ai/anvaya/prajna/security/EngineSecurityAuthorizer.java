package ai.anvaya.prajna.security;

import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.Question;

import java.util.UUID;

/**
 * Security policy and RBAC authorizer interface (ASI03).
 * Consumers can implement this interface or provide a Spring Bean to customize authorization rules.
 */
public interface EngineSecurityAuthorizer {

    void authorizeGenerate(Question question, ExplanationPolicy policy, EngineSecurityContext context);

    void authorizeReview(UUID explanationId, boolean approved, EngineSecurityContext context);

    void authorizePublish(UUID explanationId, EngineSecurityContext context);

    void authorizeFeedback(UUID explanationId, String feedbackType, EngineSecurityContext context);
}
