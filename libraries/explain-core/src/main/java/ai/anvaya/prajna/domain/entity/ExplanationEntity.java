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
@Table(name = "explanation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationEntity {

    @Id
    private UUID id;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private String status;

    private String difficulty;

    private String language;

    @Column(name = "schema_version", nullable = false)
    private String schemaVersion;

    @Column(name = "content_json", nullable = false, columnDefinition = "TEXT")
    private String contentJson;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "published_at")
    private Instant publishedAt;
}
