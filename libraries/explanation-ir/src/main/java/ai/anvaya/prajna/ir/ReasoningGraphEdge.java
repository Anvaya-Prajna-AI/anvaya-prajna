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
public class ReasoningGraphEdge {
    private String from;
    private String to;
    private String relationship; // DEPENDS_ON, DERIVED_FROM, CONTRADICTS, SUPPORTS, REQUIRES, ELIMINATES, VERIFIES
    private String justification;
}
