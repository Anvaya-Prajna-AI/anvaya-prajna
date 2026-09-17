package ai.anvaya.prajna.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request payload for expert/educator review of an explanation")
public class ReviewExplanationRequest {

    @Schema(description = "Whether the explanation is approved for publishing", example = "true")
    private Boolean approved;

    @Schema(description = "Identifier of the reviewer", example = "educator-42")
    private String reviewerId;

    @Schema(description = "Educator feedback or modification remarks", example = "Accurate step progression and visual diagram.")
    private String comment;
}
