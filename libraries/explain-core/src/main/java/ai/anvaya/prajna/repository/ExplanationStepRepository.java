package ai.anvaya.prajna.repository;

import ai.anvaya.prajna.domain.entity.ExplanationStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExplanationStepRepository extends JpaRepository<ExplanationStepEntity, UUID> {
    List<ExplanationStepEntity> findByExplanationIdOrderBySequenceAsc(UUID explanationId);
    java.util.Optional<ExplanationStepEntity> findByExplanationIdAndSequence(UUID explanationId, Integer sequence);
}
