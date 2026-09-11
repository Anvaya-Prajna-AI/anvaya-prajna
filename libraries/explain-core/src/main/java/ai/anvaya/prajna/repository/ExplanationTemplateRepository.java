package ai.anvaya.prajna.repository;

import ai.anvaya.prajna.domain.entity.ExplanationTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExplanationTemplateRepository extends JpaRepository<ExplanationTemplateEntity, UUID> {
    List<ExplanationTemplateEntity> findByDomainAndStatus(String domain, String status);
}
