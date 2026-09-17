package ai.anvaya.prajna.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request payload for submitting student or educator feedback on an explanation step")
public class FeedbackRequest {

    @Schema(description = "Optional step identifier the feedback relates to")
    private UUID stepId;

    @Schema(description = "User identifier submitting feedback", example = "student-101")
    private String userId;

    @NotBlank
    @Schema(description = "Category of feedback: HELPFUL, CONFUSING, INCORRECT, TYPO, etc.", example = "HELPFUL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String feedbackType;

    @Schema(description = "Detailed comment or suggestion", example = "The animation clarified the equation balance perfectly.")
    private String comment;
}
