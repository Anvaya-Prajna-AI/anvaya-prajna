package ai.anvaya.prajna.api.dto;

import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.Question;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request payload for generating a multi-step educational explanation")
public class GenerateExplanationRequest {

    @NotNull
    @Schema(description = "The target question details", requiredMode = Schema.RequiredMode.REQUIRED)
    private Question question;

    @Schema(description = "Pedagogical and formatting policy constraints")
    private ExplanationPolicy policy;
}
