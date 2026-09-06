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
@Table(name = "explanation_validation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationValidationEntity {

    @Id
    private UUID id;

    @Column(name = "explanation_id", nullable = false)
    private UUID explanationId;

    @Column(nullable = false)
    private String validator;

    @Column(nullable = false)
    private String status;

    private Double score;

    @Lob
    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
