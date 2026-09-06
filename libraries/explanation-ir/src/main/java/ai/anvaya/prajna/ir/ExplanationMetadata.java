package ai.anvaya.prajna.ir;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExplanationMetadata {
    private String language;
    private StudentLevel studentLevel;
    private String modelVersion;
    private String compilerVersion;
    private Long generationLatencyMs;
    private Double qualityScore;
}
