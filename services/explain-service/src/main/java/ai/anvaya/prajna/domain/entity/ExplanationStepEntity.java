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

import java.util.UUID;

@Entity
@Table(name = "explanation_step")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationStepEntity {

    @Id
    private UUID id;

    @Column(name = "explanation_id", nullable = false)
    private UUID explanationId;

    @Column(nullable = false)
    private Integer sequence;

    @Column(name = "step_type", nullable = false)
    private String stepType;

    @Lob
    @Column(name = "content_json", nullable = false, columnDefinition = "TEXT")
    private String contentJson;

    @Column(name = "validation_status")
    private String validationStatus;
}
