package ai.anvaya.prajna.domain.plugin;

import ai.anvaya.prajna.ir.Question;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Registry of available explanation reasoning domain plugins.
 * Prioritizes specific symbolic domain plugins first, and seamlessly falls back
 * to the GeneralExamReasoningDomain for open-ended competitive exam subjects (Issue #348).
 */
@Component
public class ExplanationDomainRegistry {

    public static final String FALLBACK_DOMAIN = "GENERAL_EXAM";

    private final List<ExplanationDomain> domains;

    public ExplanationDomainRegistry(List<ExplanationDomain> domains) {
        this.domains = domains;
    }

    public Optional<ExplanationDomain> findDomain(Question question) {
        if (question == null) {
            return Optional.empty();
        }

        // 1. Prioritize specific domain plugins first
        Optional<ExplanationDomain> specific = domains.stream()
                .filter(d -> !d.getDomainName().equalsIgnoreCase(FALLBACK_DOMAIN) && d.supports(question))
                .findFirst();
        if (specific.isPresent()) {
            return specific;
        }

        // 2. Fall back to GeneralExamReasoningDomain
        return domains.stream()
                .filter(d -> d.getDomainName().equalsIgnoreCase(FALLBACK_DOMAIN) && d.supports(question))
                .findFirst();
    }

    public Optional<ExplanationDomain> getDomainByName(String domainName) {
        if (domainName == null) return Optional.empty();
        return domains.stream()
                .filter(d -> d.getDomainName().equalsIgnoreCase(domainName))
                .findFirst();
    }

    public List<ExplanationDomain> getAllDomains() {
        return domains;
    }
}
