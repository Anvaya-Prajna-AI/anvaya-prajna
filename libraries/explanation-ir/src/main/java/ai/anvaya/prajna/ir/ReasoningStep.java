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
public class ReasoningStep {
    private String id;
    private Integer sequence;
    private StepType type;
    private List<String> inputs;
    private StepOperation operation;
    private String before;
    private String after;
    private List<String> outputs;
    private StepJustification justification;
    private List<RepresentationType> representations;
    private MathExpression math;
    private VerificationResult verification;
    private Misconception misconception;
}
