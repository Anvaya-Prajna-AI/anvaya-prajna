package ai.anvaya.prajna.security;

import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.Question;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeycloakSecurityTest {

    private final KeycloakJwtAuthenticationConverter converter = new KeycloakJwtAuthenticationConverter();
    private final KeycloakEngineSecurityAuthorizer authorizer = new KeycloakEngineSecurityAuthorizer();
    private final KeycloakSecurityContextFilter filter = new KeycloakSecurityContextFilter(converter);

    @BeforeEach
    @AfterEach
    void cleanContext() {
        SecurityContextHolder.clearContext();
        EngineSecurityContextHolder.clearContext();
    }

    @Test
    void shouldExtractAuthoritiesAndClaimsFromKeycloakJwt() {
        Jwt jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .claim("preferred_username", "student_john")
                .claim("tenant_id", "nag-tenant-1")
                .claim("realm_access", Map.of("roles", List.of("STUDENT", "LEARNER")))
                .claim("resource_access", Map.of("nag-client", Map.of("roles", List.of("PRACTICE_USER"))))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        Collection<GrantedAuthority> authorities = converter.extractAuthorities(jwt);
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_STUDENT", "ROLE_LEARNER", "ROLE_PRACTICE_USER");

        assertThat(converter.extractPrincipalName(jwt)).isEqualTo("student_john");
        assertThat(converter.extractTenantId(jwt)).isEqualTo("nag-tenant-1");
    }

    @Test
    void shouldDefaultToRoleStudentWhenRolesEmptyInJwt() {
        Jwt jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .claim("preferred_username", "anon_learner")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        Collection<GrantedAuthority> authorities = converter.extractAuthorities(jwt);
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(EngineCallerPrincipal.ROLE_STUDENT);
        assertThat(converter.extractTenantId(jwt)).isEqualTo("default");
    }

    @Test
    void shouldEnforceAsi03AuthorizerPermissions() {
        UUID explanationId = UUID.randomUUID();

        // Student context
        EngineCallerPrincipal student = EngineCallerPrincipal.of("user-1", "tenant-1", "ROLE_STUDENT");
        // Educator context
        EngineCallerPrincipal educator = EngineCallerPrincipal.of("teacher-1", "tenant-1", "ROLE_EDUCATOR");
        // Instructor context
        EngineCallerPrincipal instructor = EngineCallerPrincipal.of("inst-1", "tenant-1", "ROLE_INSTRUCTOR");
        // Admin context
        EngineCallerPrincipal admin = EngineCallerPrincipal.of("admin-1", "tenant-1", "ROLE_ADMIN");

        // Review authorizations
        assertThatThrownBy(() -> authorizer.authorizeReview(explanationId, true, student))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("ASI03");

        assertThatCode(() -> authorizer.authorizeReview(explanationId, true, educator)).doesNotThrowAnyException();
        assertThatCode(() -> authorizer.authorizeReview(explanationId, true, instructor)).doesNotThrowAnyException();
        assertThatCode(() -> authorizer.authorizeReview(explanationId, true, admin)).doesNotThrowAnyException();

        // Publish authorizations
        assertThatThrownBy(() -> authorizer.authorizePublish(explanationId, student))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("ASI03");
        assertThatThrownBy(() -> authorizer.authorizePublish(explanationId, educator))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("ASI03");
        assertThatCode(() -> authorizer.authorizePublish(explanationId, admin)).doesNotThrowAnyException();

        // Generate and feedback
        assertThatCode(() -> authorizer.authorizeGenerate(Question.builder().build(), ExplanationPolicy.builder().build(), student))
                .doesNotThrowAnyException();
        assertThatCode(() -> authorizer.authorizeFeedback(explanationId, "HELPFUL", student))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldPopulateSecurityContextFromJwtTokenInFilter() throws Exception {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .claim("preferred_username", "educator_alice")
                .claim("tenant_id", "tenant-delhi")
                .claim("realm_access", Map.of("roles", List.of("EDUCATOR")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        JwtAuthenticationToken authToken = (JwtAuthenticationToken) converter.convert(jwt);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        final EngineSecurityContext[] capturedContext = new EngineSecurityContext[1];
        FilterChain chain = (req, res) -> {
            capturedContext[0] = EngineSecurityContextHolder.getContext();
        };

        filter.doFilter(request, response, chain);

        assertThat(capturedContext[0]).isNotNull();
        assertThat(capturedContext[0].getUserId()).isEqualTo("educator_alice");
        assertThat(capturedContext[0].getTenantId()).isEqualTo("tenant-delhi");
        assertThat(capturedContext[0].hasRole("EDUCATOR")).isTrue();
        assertThat(capturedContext[0].isAuthenticated()).isTrue();

        // Cleared in finally
        assertThat(EngineSecurityContextHolder.getContext().isAuthenticated()).isFalse();
        assertThat(EngineSecurityContextHolder.getContext().getUserId()).isEqualTo("anonymous");
    }

    @Test
    void shouldFallbackToGatewayHeadersInFilterWhenJwtAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(KeycloakSecurityContextFilter.HEADER_USER_ID, "gateway_user");
        request.addHeader(KeycloakSecurityContextFilter.HEADER_TENANT_ID, "gateway_tenant");
        request.addHeader(KeycloakSecurityContextFilter.HEADER_ROLES, "ADMIN,EDUCATOR");
        MockHttpServletResponse response = new MockHttpServletResponse();

        final EngineSecurityContext[] capturedContext = new EngineSecurityContext[1];
        FilterChain chain = (req, res) -> {
            capturedContext[0] = EngineSecurityContextHolder.getContext();
        };

        filter.doFilter(request, response, chain);

        assertThat(capturedContext[0]).isNotNull();
        assertThat(capturedContext[0].getUserId()).isEqualTo("gateway_user");
        assertThat(capturedContext[0].getTenantId()).isEqualTo("gateway_tenant");
        assertThat(capturedContext[0].hasRole("ADMIN")).isTrue();
        assertThat(capturedContext[0].hasRole("EDUCATOR")).isTrue();
        assertThat(capturedContext[0].isAuthenticated()).isTrue();
    }
}
