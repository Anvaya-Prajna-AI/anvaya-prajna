package ai.anvaya.prajna.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "explanation_feedback")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationFeedbackEntity {

    @Id
    private UUID id;

    @Column(name = "explanation_id", nullable = false)
    private UUID explanationId;

    @Column(name = "step_id")
    private UUID stepId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "feedback_type", nullable = false)
    private String feedbackType;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
