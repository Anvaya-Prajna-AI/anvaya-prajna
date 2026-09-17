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
@Table(name = "explanation_template")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationTemplateEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String domain;

    @Column(name = "template_type", nullable = false)
    private String templateType;

    @Column(name = "content_json", nullable = false, columnDefinition = "TEXT")
    private String contentJson;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private String status;
}
