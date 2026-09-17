package ai.anvaya.prajna.validation;

import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.reasoning.ReasoningProposal;

public interface ProposalValidator {
    String getName();
    ValidationResult validate(Question question, ReasoningProposal proposal);
}
