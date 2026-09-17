package ai.anvaya.prajna.repository;

import ai.anvaya.prajna.domain.entity.ExplanationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExplanationRepository extends JpaRepository<ExplanationEntity, UUID> {
    Optional<ExplanationEntity> findFirstByQuestionIdOrderByVersionDesc(String questionId);
    List<ExplanationEntity> findAllByQuestionIdOrderByVersionDesc(String questionId);
}
