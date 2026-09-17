package ai.anvaya.prajna.security;

import java.util.Set;

/**
 * Caller security context abstraction (ASI03: Identity and Privilege Abuse defense).
 * Allows any consumer application of this library to pass authenticated identity,
 * tenant scoping, and fine-grained roles (RBAC/ABAC) without hardcoding IAM into the engine.
 */
public interface EngineSecurityContext {

    String getUserId();

    String getTenantId();

    Set<String> getRoles();

    boolean hasRole(String role);

    boolean isAuthenticated();
}
