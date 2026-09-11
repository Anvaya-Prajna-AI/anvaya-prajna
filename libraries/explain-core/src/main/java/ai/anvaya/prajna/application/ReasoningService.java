package ai.anvaya.prajna.application;

import ai.anvaya.prajna.ai.ReasoningPlanner;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import org.springframework.stereotype.Service;

@Service
public class ReasoningService {

    private final ReasoningPlanner reasoningPlanner;

    public ReasoningService(ReasoningPlanner reasoningPlanner) {
        this.reasoningPlanner = reasoningPlanner;
    }

    public ReasoningProposal generateReasoning(Question question) {
        return reasoningPlanner.plan(question);
    }
}
