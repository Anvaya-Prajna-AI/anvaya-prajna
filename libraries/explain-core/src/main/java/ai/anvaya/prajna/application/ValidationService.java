package ai.anvaya.prajna.application;

import ai.anvaya.prajna.domain.entity.ExplanationValidationEntity;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import ai.anvaya.prajna.repository.ExplanationValidationRepository;
import ai.anvaya.prajna.validation.ValidationPipeline;
import ai.anvaya.prajna.validation.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ValidationService {

    private final ValidationPipeline validationPipeline;
    private final ExplanationValidationRepository validationRepository;
    private final ObjectMapper objectMapper;

    public ValidationService(ExplanationValidationRepository validationRepository,
                             ObjectMapper objectMapper) {
        this.validationPipeline = new ValidationPipeline();
        this.validationRepository = validationRepository;
        this.objectMapper = objectMapper;
    }

    public ValidationResult validateProposal(Question question, ReasoningProposal proposal) {
        return validationPipeline.validate(question, proposal);
    }

    public void recordValidationResult(UUID explanationId, ValidationResult result) {
        try {
            String detailsJson = objectMapper.writeValueAsString(result.getViolations());
            ExplanationValidationEntity entity = ExplanationValidationEntity.builder()
                    .id(UUID.randomUUID())
                    .explanationId(explanationId)
                    .validator("ValidationPipeline")
                    .status(result.getStatus().name())
                    .score(result.getScore())
                    .detailsJson(detailsJson)
                    .createdAt(Instant.now())
                    .build();
            validationRepository.save(entity);
        } catch (Exception ignored) {}
    }
}
