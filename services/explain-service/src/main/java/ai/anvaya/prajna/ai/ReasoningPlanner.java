package ai.anvaya.prajna.ai;

import ai.anvaya.prajna.domain.plugin.ExplanationDomain;
import ai.anvaya.prajna.domain.plugin.ExplanationDomainRegistry;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
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

        // LLM fallback via LiteLLM / Spring AI ChatModel
        if (chatModel != null) {
            try {
                String systemPrompt = promptRepository.getPrompt("reasoning-proposal.prompt");
                String userPrompt = "Generate step-by-step reasoning for question: " + question.getStatement();
                String targetModel = modelRouter.routeModel(question.getDomain(), question.getDifficulty());

                Prompt prompt = new Prompt(
                        List.of(
                                new SystemMessage(systemPrompt),
                                new UserMessage(userPrompt)
                        ),
                        OpenAiChatOptions.builder()
                                .model(targetModel)
                                .temperature(0.2)
                                .build()
                );

                ChatResponse chatResponse = chatModel.call(prompt);
                if (chatResponse != null && chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                    String responseText = chatResponse.getResult().getOutput().getText();
                    if (responseText != null && !responseText.isBlank()) {
                        return objectMapper.readValue(responseText, ReasoningProposal.class);
                    }
                }
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
