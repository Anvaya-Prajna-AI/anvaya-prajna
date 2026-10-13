package ai.anvaya.prajna.security;

/**
 * @deprecated Use {@link KeycloakSecurityContextFilter} instead.
 */
@Deprecated
public final class ExampleSecurityContextFilter {
    public static final String HEADER_USER_ID = KeycloakSecurityContextFilter.HEADER_USER_ID;
    public static final String HEADER_TENANT_ID = KeycloakSecurityContextFilter.HEADER_TENANT_ID;
    public static final String HEADER_ROLES = KeycloakSecurityContextFilter.HEADER_ROLES;

    private ExampleSecurityContextFilter() {
    }
}
