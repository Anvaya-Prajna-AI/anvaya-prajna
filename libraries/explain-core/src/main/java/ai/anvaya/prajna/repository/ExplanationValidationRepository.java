package ai.anvaya.prajna.repository;

import ai.anvaya.prajna.domain.entity.ExplanationValidationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExplanationValidationRepository extends JpaRepository<ExplanationValidationEntity, UUID> {
    List<ExplanationValidationEntity> findByExplanationIdOrderByCreatedAtDesc(UUID explanationId);
}
