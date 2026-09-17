package ai.anvaya.prajna.domain.plugin;

import ai.anvaya.prajna.ir.Question;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ExplanationDomainRegistry {

    private final List<ExplanationDomain> domains;

    public ExplanationDomainRegistry(List<ExplanationDomain> domains) {
        this.domains = domains;
    }

    public Optional<ExplanationDomain> findDomain(Question question) {
        if (question == null) {
            return Optional.empty();
        }
        return domains.stream()
                .filter(d -> d.supports(question))
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
