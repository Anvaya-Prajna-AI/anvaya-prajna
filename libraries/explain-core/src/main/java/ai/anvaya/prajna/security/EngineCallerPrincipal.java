package ai.anvaya.prajna.security;

import java.util.Collections;
import java.util.Set;

/**
 * Immutable value object representing the verified caller principal.
 */
public class EngineCallerPrincipal implements EngineSecurityContext {

    public static final String ROLE_STUDENT = "ROLE_STUDENT";
    public static final String ROLE_EDUCATOR = "ROLE_EDUCATOR";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_SYSTEM = "ROLE_SYSTEM";

    private final String userId;
    private final String tenantId;
    private final Set<String> roles;
    private final boolean authenticated;

    public EngineCallerPrincipal(String userId, String tenantId, Set<String> roles, boolean authenticated) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.roles = roles != null ? Collections.unmodifiableSet(roles) : Collections.emptySet();
        this.authenticated = authenticated;
    }

    public static EngineCallerPrincipal anonymous() {
        return new EngineCallerPrincipal("anonymous", "default", Set.of(ROLE_STUDENT), false);
    }

    public static EngineCallerPrincipal system() {
        return new EngineCallerPrincipal("system", "default", Set.of(ROLE_SYSTEM, ROLE_ADMIN), true);
    }

    public static EngineCallerPrincipal of(String userId, String tenantId, String... roles) {
        return new EngineCallerPrincipal(userId, tenantId, Set.of(roles), true);
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public boolean hasRole(String role) {
        if (role == null) return false;
        return roles.contains(role) || roles.contains("ROLE_" + role);
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }
}
