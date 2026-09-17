package ai.anvaya.prajna.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Example Reference Security Filter demonstrating how consumer applications
 * map HTTP identity / gateway claims into the Anvaya-Prajna EngineSecurityContext (ASI03).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExampleSecurityContextFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_ROLES = "X-User-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader(HEADER_USER_ID);
        String tenantId = request.getHeader(HEADER_TENANT_ID);
        String rolesHeader = request.getHeader(HEADER_ROLES);

        if (tenantId == null || tenantId.isBlank()) {
            tenantId = "default";
        }

        Set<String> roles;
        boolean authenticated;

        if (rolesHeader != null && !rolesHeader.isBlank()) {
            roles = Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .collect(Collectors.toSet());
            authenticated = true;
            if (userId == null || userId.isBlank()) {
                userId = "authenticated-user";
            }
        } else if (userId != null && !userId.isBlank()) {
            roles = Set.of(EngineCallerPrincipal.ROLE_STUDENT);
            authenticated = true;
        } else {
            // Unauthenticated / default mode: provides standard roles for backward compatibility in reference service
            roles = Set.of(
                    EngineCallerPrincipal.ROLE_STUDENT,
                    EngineCallerPrincipal.ROLE_EDUCATOR,
                    EngineCallerPrincipal.ROLE_ADMIN
            );
            userId = "system";
            authenticated = false;
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
