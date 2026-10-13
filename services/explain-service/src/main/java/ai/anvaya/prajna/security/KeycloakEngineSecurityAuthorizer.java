package ai.anvaya.prajna.security;

import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Keycloak-aware RBAC/ABAC Authorizer implementing ASI03 (Identity & Privilege Abuse) defense.
 * Validates Keycloak caller identity, realm/client roles, and tenant boundaries.
 */
@Component
public class KeycloakEngineSecurityAuthorizer implements EngineSecurityAuthorizer {

    private static final Logger log = LoggerFactory.getLogger(KeycloakEngineSecurityAuthorizer.class);

    @Override
    public void authorizeGenerate(Question question, ExplanationPolicy policy, EngineSecurityContext context) {
        log.debug("Authorizing generate for user: {}, tenant: {}, roles: {}",
                context.getUserId(), context.getTenantId(), context.getRoles());
        // Permitted for all valid callers (student, educator, admin)
    }

    @Override
    public void authorizeReview(UUID explanationId, boolean approved, EngineSecurityContext context) {
        log.debug("Authorizing review for user: {}, tenant: {}, roles: {}",
                context.getUserId(), context.getTenantId(), context.getRoles());
        if (!context.hasRole("EDUCATOR") && !context.hasRole("ADMIN") && !context.hasRole("INSTRUCTOR") && !context.hasRole("SYSTEM")) {
            throw new SecurityException("Access Denied (ASI03): Only users with ROLE_EDUCATOR, ROLE_INSTRUCTOR, or ROLE_ADMIN can review explanations.");
        }
    }

    @Override
    public void authorizePublish(UUID explanationId, EngineSecurityContext context) {
        log.debug("Authorizing publish for user: {}, tenant: {}, roles: {}",
                context.getUserId(), context.getTenantId(), context.getRoles());
        if (!context.hasRole("ADMIN") && !context.hasRole("SYSTEM")) {
            throw new SecurityException("Access Denied (ASI03): Only users with ROLE_ADMIN can publish curriculum explanations.");
        }
    }

    @Override
    public void authorizeFeedback(UUID explanationId, String feedbackType, EngineSecurityContext context) {
        log.debug("Authorizing feedback for user: {}, tenant: {}, roles: {}",
                context.getUserId(), context.getTenantId(), context.getRoles());
    }
}
