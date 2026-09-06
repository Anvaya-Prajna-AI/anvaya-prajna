package ai.anvaya.prajna.ai;

import ai.anvaya.prajna.domain.plugin.ExplanationDomain;
import ai.anvaya.prajna.domain.plugin.ExplanationDomainRegistry;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReasoningPlanner {

    private final ExplanationDomainRegistry domainRegistry;
    private final PromptRepository promptRepository;
    private final ModelRouter modelRouter;
    private final ObjectMapper objectMapper;
    private final ChatModel chatModel;

    @Autowired
    public ReasoningPlanner(ExplanationDomainRegistry domainRegistry,
                            PromptRepository promptRepository,
                            ModelRouter modelRouter,
                            ObjectMapper objectMapper,
                            @Autowired(required = false) ChatModel chatModel) {
        this.domainRegistry = domainRegistry;
        this.promptRepository = promptRepository;
        this.modelRouter = modelRouter;
        this.objectMapper = objectMapper;
        this.chatModel = chatModel;
    }

    public ReasoningProposal plan(Question question) {
        // Check if there is a matching domain plugin
        Optional<ExplanationDomain> domainOpt = domainRegistry.findDomain(question);

        // If LLM is configured and not running purely deterministic, we could call LLM or domain plugin
        if (domainOpt.isPresent()) {
            return domainOpt.get().generateReasoning(question);
        }

        // LLM fallback if chatModel available
        if (chatModel != null) {
            try {
                String systemPrompt = promptRepository.getPrompt("reasoning-proposal.prompt");
                String userPrompt = "Generate step-by-step reasoning for question: " + question.getStatement();
                String response = chatModel.call(userPrompt);
                return objectMapper.readValue(response, ReasoningProposal.class);
            } catch (Exception ignored) {
            }
        }

        // Generic fallback
        return ReasoningProposal.builder()
                .questionId(question.getQuestionId())
                .problemSummary(question.getStatement())
                .conclusion(question.getAuthoritativeAnswer() != null ? question.getAuthoritativeAnswer() : "Solution")
                .build();
    }
}
