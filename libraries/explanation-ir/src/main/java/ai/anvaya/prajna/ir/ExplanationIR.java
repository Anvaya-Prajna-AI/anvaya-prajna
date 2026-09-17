package ai.anvaya.prajna.ir;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExplanationIR {
    @Builder.Default
    private String schemaVersion = "1.0";
    private String explanationId;
    private String questionId;
    @Builder.Default
    private ExplanationStatus status = ExplanationStatus.DRAFT;
    private Problem problem;
    private List<String> concepts;
    private List<String> facts;
    private List<String> assumptions;
    private ReasoningGraphIR reasoningGraph;
    private List<ReasoningStep> steps;
    private VerificationResult verification;
    private List<Hint> hints;
    private List<Misconception> misconceptions;
    private List<DiagramIR> diagrams;
    private AnimationIR animation;
    private ExplanationMetadata metadata;
}
