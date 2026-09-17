package ai.anvaya.prajna.validation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationResult {
    @Builder.Default
    private ValidationStatus status = ValidationStatus.PASSED;
    @Builder.Default
    private Double score = 1.0;
    @Builder.Default
    private List<ValidationViolation> violations = new ArrayList<>();

    public boolean isPassed() {
        return status == ValidationStatus.PASSED;
    }
}
