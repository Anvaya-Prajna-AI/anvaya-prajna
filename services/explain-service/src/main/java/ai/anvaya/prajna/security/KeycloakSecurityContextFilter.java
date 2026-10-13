package ai.anvaya.prajna.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Keycloak JWT and API Gateway claims security filter (ASI03: Identity and Privilege Abuse defense).
 * Extracts identity and tenant scoping from Keycloak JwtAuthenticationToken when present,
 * and gracefully falls back to verified API Gateway headers (X-User-Id, X-Tenant-Id, X-User-Roles).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class KeycloakSecurityContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(KeycloakSecurityContextFilter.class);

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_ROLES = "X-User-Roles";

    private final KeycloakJwtAuthenticationConverter jwtConverter;

    public KeycloakSecurityContextFilter(KeycloakJwtAuthenticationConverter jwtConverter) {
        this.jwtConverter = jwtConverter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = null;
        String tenantId = null;
        Set<String> roles = null;
        boolean authenticated = false;

        // 1. Try extracting from Spring Security's SecurityContext (Keycloak JWT)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth && auth.isAuthenticated()) {
            Jwt jwt = jwtAuth.getToken();
            userId = jwtConverter.extractPrincipalName(jwt);
            tenantId = jwtConverter.extractTenantId(jwt);
            roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            authenticated = true;
            log.debug("Resolved caller from Keycloak JWT: user={}, tenant={}, roles={}", userId, tenantId, roles);
        }

        // 2. Gateway header fallback if not authenticated via JWT
        if (!authenticated) {
            String headerUserId = request.getHeader(HEADER_USER_ID);
            String headerTenantId = request.getHeader(HEADER_TENANT_ID);
            String headerRoles = request.getHeader(HEADER_ROLES);

            if (headerTenantId != null && !headerTenantId.isBlank()) {
                tenantId = headerTenantId.trim();
            } else {
                tenantId = "default";
            }

            if (headerRoles != null && !headerRoles.isBlank()) {
                roles = Arrays.stream(headerRoles.split(","))
                        .map(String::trim)
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .collect(Collectors.toSet());
                userId = (headerUserId != null && !headerUserId.isBlank()) ? headerUserId.trim() : "authenticated-user";
                authenticated = true;
                log.debug("Resolved caller from gateway headers: user={}, tenant={}, roles={}", userId, tenantId, roles);
            } else if (headerUserId != null && !headerUserId.isBlank()) {
                userId = headerUserId.trim();
                roles = Set.of(EngineCallerPrincipal.ROLE_STUDENT);
                authenticated = true;
                log.debug("Resolved student caller from user header: user={}, tenant={}", userId, tenantId);
            } else {
                // Unauthenticated / default reference mode: provides standard roles for backward compatibility
                roles = Set.of(
                        EngineCallerPrincipal.ROLE_STUDENT,
                        EngineCallerPrincipal.ROLE_EDUCATOR,
                        EngineCallerPrincipal.ROLE_ADMIN
                );
                userId = "system";
                tenantId = (tenantId != null) ? tenantId : "default";
                authenticated = false;
            }
        }

        EngineCallerPrincipal principal = new EngineCallerPrincipal(userId, tenantId, roles, authenticated);
        EngineSecurityContextHolder.setContext(principal);

        try {
            filterChain.doFilter(request, response);
        } finally {
            EngineSecurityContextHolder.clearContext();
        }
    }
}
