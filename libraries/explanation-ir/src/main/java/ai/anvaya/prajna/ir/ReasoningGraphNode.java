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
public class ReasoningGraphNode {
    private String id;
    private String type; // FACT, RULE, RESULT, CONCLUSION, VERIFICATION
    private String content;
    private String source; // GIVEN, AI, DETERMINISTIC
    private Double confidence;
    private String validationStatus;
}
