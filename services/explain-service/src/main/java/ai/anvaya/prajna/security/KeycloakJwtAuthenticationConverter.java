package ai.anvaya.prajna.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts Keycloak OAuth2 / OIDC JWT tokens into Spring Security Authentication (Issue #348).
 * Extracts preferred_username, tenant_id, realm_access.roles, and resource_access roles.
 */
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String principalName = extractPrincipalName(jwt);
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    public Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Keycloak realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof List<?> list) {
                for (Object r : list) {
                    if (r != null) {
                        String role = r.toString().trim().toUpperCase();
                        authorities.add(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
                    }
                }
            }
        }

        // 2. Keycloak resource_access.*.roles
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            for (Object clientObj : resourceAccess.values()) {
                if (clientObj instanceof Map<?, ?> clientMap && clientMap.containsKey("roles")) {
                    Object rolesObj = clientMap.get("roles");
                    if (rolesObj instanceof List<?> list) {
                        for (Object r : list) {
                            if (r != null) {
                                String role = r.toString().trim().toUpperCase();
                                authorities.add(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
                            }
                        }
                    }
                }
            }
        }

        // 3. Top-level 'roles' claim
        List<String> topRoles = jwt.getClaimAsStringList("roles");
        if (topRoles != null) {
            for (String r : topRoles) {
                if (r != null) {
                    String role = r.trim().toUpperCase();
                    authorities.add(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
                }
            }
        }

        // 4. Default ROLE_STUDENT if empty
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority(EngineCallerPrincipal.ROLE_STUDENT));
        }

        return authorities;
    }

    public String extractPrincipalName(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (username != null && !username.isBlank()) {
            return username;
        }
        String sub = jwt.getSubject();
        if (sub != null && !sub.isBlank()) {
            return sub;
        }
        return "authenticated-user";
    }

    public String extractTenantId(Jwt jwt) {
        String tenant = jwt.getClaimAsString("tenant_id");
        return (tenant != null && !tenant.isBlank()) ? tenant : "default";
    }
}