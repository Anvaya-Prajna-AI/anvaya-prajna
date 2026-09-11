package ai.anvaya.prajna.security;

/**
 * ThreadLocal holder for propagating caller identity across library execution (ASI03).
 */
public final class EngineSecurityContextHolder {

    private static final ThreadLocal<EngineSecurityContext> CONTEXT = new ThreadLocal<>();

    private EngineSecurityContextHolder() {}

    public static void setContext(EngineSecurityContext context) {
        CONTEXT.set(context);
    }

    public static EngineSecurityContext getContext() {
        EngineSecurityContext ctx = CONTEXT.get();
        return ctx != null ? ctx : EngineCallerPrincipal.anonymous();
    }

    public static void clearContext() {
        CONTEXT.remove();
    }
}
