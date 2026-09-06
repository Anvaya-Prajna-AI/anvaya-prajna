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
public class ExplanationPolicy {
    @Builder.Default
    private StudentLevel level = StudentLevel.INTERMEDIATE;
    @Builder.Default
    private Boolean showFormula = true;
    @Builder.Default
    private Boolean showEveryCalculation = true;
    @Builder.Default
    private Boolean showWhy = true;
    @Builder.Default
    private Boolean showVerification = true;
    @Builder.Default
    private Boolean showMisconceptions = true;
    @Builder.Default
    private String animation = "LIGHT"; // NONE, LIGHT, FULL
}
