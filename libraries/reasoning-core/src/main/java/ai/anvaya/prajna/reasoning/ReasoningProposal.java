package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.DiagramIR;
import ai.anvaya.prajna.ir.Hint;
import ai.anvaya.prajna.ir.Misconception;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.VerificationResult;
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
public class ReasoningProposal {
    private String questionId;
    private String problemSummary;
    private List<String> concepts;
    private List<String> facts;
    private List<String> assumptions;
    private List<ReasoningStep> steps;
    private String conclusion;
    private VerificationResult verification;
    private List<Hint> hints;
    private List<Misconception> misconceptions;
    private List<DiagramIR> diagrams;
}
