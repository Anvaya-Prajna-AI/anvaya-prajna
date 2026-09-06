package ai.anvaya.prajna.repository;

import ai.anvaya.prajna.domain.entity.ExplanationFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExplanationFeedbackRepository extends JpaRepository<ExplanationFeedbackEntity, UUID> {
    List<ExplanationFeedbackEntity> findByExplanationIdOrderByCreatedAtDesc(UUID explanationId);
}
