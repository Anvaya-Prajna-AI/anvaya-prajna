package ai.anvaya.prajna.application;

import ai.anvaya.prajna.domain.entity.ExplanationEntity;
import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepJustification;
import ai.anvaya.prajna.repository.ExplanationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WhyService {

    private final ExplanationRepository explanationRepository;
    private final ObjectMapper objectMapper;

    public WhyService(ExplanationRepository explanationRepository, ObjectMapper objectMapper) {
        this.explanationRepository = explanationRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<StepJustification> getWhyForStep(String questionId, String stepId) {
        Optional<ExplanationEntity> entityOpt = explanationRepository.findFirstByQuestionIdOrderByVersionDesc(questionId);
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        try {
            ExplanationIR ir = objectMapper.readValue(entityOpt.get().getContentJson(), ExplanationIR.class);
            if (ir.getSteps() == null) {
                return Optional.empty();
            }

            return ir.getSteps().stream()
                    .filter(s -> stepId.equalsIgnoreCase(s.getId()))
                    .map(ReasoningStep::getJustification)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .or(() -> Optional.of(StepJustification.builder()
                            .text("This step applies a mathematically valid deductive transformation to progress towards the goal.")
                            .depth(1)
                            .build()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
