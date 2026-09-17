package ai.anvaya.prajna.domain.plugin;

import ai.anvaya.prajna.ir.Hint;
import ai.anvaya.prajna.ir.Misconception;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import ai.anvaya.prajna.validation.ValidationResult;

import java.util.List;

public interface ExplanationDomain {
    boolean supports(Question question);
    String getDomainName();
    ReasoningProposal generateReasoning(Question question);
    ValidationResult validate(ReasoningProposal proposal);
    List<Hint> generateHints(ReasoningProposal proposal);
    List<Misconception> detectMisconceptions(ReasoningProposal proposal);
}
