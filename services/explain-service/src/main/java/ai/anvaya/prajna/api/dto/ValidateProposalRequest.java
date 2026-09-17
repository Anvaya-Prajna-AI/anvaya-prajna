package ai.anvaya.prajna.api.dto;

import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
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
@Schema(description = "Request payload for validating an AI/domain reasoning proposal against security and correctness gates")
public class ValidateProposalRequest {

    @Schema(description = "The target question details")
    private Question question;

    @NotNull
    @Schema(description = "The proposed reasoning chain and steps to validate", requiredMode = Schema.RequiredMode.REQUIRED)
    private ReasoningProposal proposal;
}
