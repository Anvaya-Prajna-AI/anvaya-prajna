package ai.anvaya.prajna.security;

import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Default RBAC Authorizer implementation (ASI03: Identity and Privilege Abuse defense).
 * Enforces role-based permissions:
 * - Anyone can generate (Student, Educator, Admin, Anonymous)
 * - Only Educator or Admin can Review
 * - Only Admin can Publish
 * - Any authenticated or student user can submit feedback
 */
@Component
@ConditionalOnMissingBean(EngineSecurityAuthorizer.class)
public class DefaultEngineSecurityAuthorizer implements EngineSecurityAuthorizer {

    private static final Logger log = LoggerFactory.getLogger(DefaultEngineSecurityAuthorizer.class);

    @Override
    public void authorizeGenerate(Question question, ExplanationPolicy policy, EngineSecurityContext context) {
        log.debug("Authorizing generate for user: {}, roles: {}", context.getUserId(), context.getRoles());
        // Default: Permitted for all
    }

    @Override
    public void authorizeReview(UUID explanationId, boolean approved, EngineSecurityContext context) {
        log.debug("Authorizing review for user: {}, roles: {}", context.getUserId(), context.getRoles());
        if (!context.hasRole("EDUCATOR") && !context.hasRole("ADMIN") && !context.hasRole("SYSTEM")) {
            throw new SecurityException("Access Denied (ASI03): Only users with ROLE_EDUCATOR or ROLE_ADMIN can review explanations.");
        }
    }

    @Override
    public void authorizePublish(UUID explanationId, EngineSecurityContext context) {
        log.debug("Authorizing publish for user: {}, roles: {}", context.getUserId(), context.getRoles());
        if (!context.hasRole("ADMIN") && !context.hasRole("SYSTEM")) {
            throw new SecurityException("Access Denied (ASI03): Only users with ROLE_ADMIN can publish curriculum explanations.");
        }
    }

    @Override
    public void authorizeFeedback(UUID explanationId, String feedbackType, EngineSecurityContext context) {
        log.debug("Authorizing feedback for user: {}, roles: {}", context.getUserId(), context.getRoles());
        // Default: Permitted
    }
}
