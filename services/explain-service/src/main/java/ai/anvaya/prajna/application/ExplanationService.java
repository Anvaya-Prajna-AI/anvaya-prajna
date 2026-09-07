package ai.anvaya.prajna.application;

import ai.anvaya.prajna.cache.ExplanationCacheService;
import ai.anvaya.prajna.domain.entity.ExplanationEntity;
import ai.anvaya.prajna.domain.entity.ExplanationFeedbackEntity;
import ai.anvaya.prajna.domain.entity.ExplanationStepEntity;
import ai.anvaya.prajna.exception.ExplanationNotFoundException;
import ai.anvaya.prajna.exception.ValidationFailedException;
import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.ExplanationStatus;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import ai.anvaya.prajna.repository.ExplanationFeedbackRepository;
import ai.anvaya.prajna.repository.ExplanationRepository;
import ai.anvaya.prajna.repository.ExplanationStepRepository;
import ai.anvaya.prajna.validation.ValidationResult;
import ai.anvaya.prajna.validation.ValidationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExplanationService {

    private static final Logger log = LoggerFactory.getLogger(ExplanationService.class);

    private final ReasoningService reasoningService;
    private final ValidationService validationService;
    private final CompilationService compilationService;
    private final ExplanationRepository explanationRepository;
    private final ExplanationStepRepository stepRepository;
    private final ExplanationFeedbackRepository feedbackRepository;
    private final ExplanationCacheService cacheService;
    private final ObjectMapper objectMapper;

    public ExplanationService(ReasoningService reasoningService,
                              ValidationService validationService,
                              CompilationService compilationService,
                              ExplanationRepository explanationRepository,
                              ExplanationStepRepository stepRepository,
                              ExplanationFeedbackRepository feedbackRepository,
                              ExplanationCacheService cacheService,
                              ObjectMapper objectMapper) {
        this.reasoningService = reasoningService;
        this.validationService = validationService;
        this.compilationService = compilationService;
        this.explanationRepository = explanationRepository;
        this.stepRepository = stepRepository;
        this.feedbackRepository = feedbackRepository;
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ExplanationIR generateExplanation(Question question, ExplanationPolicy policy) {
        if (question.getQuestionId() == null || question.getQuestionId().isBlank()) {
            question.setQuestionId(UUID.randomUUID().toString());
        }

        log.info("generateExplanation called for questionId: {}", question.getQuestionId());

        String cacheKey = cacheService.computeCacheKey(question.getQuestionId(), policy);
        Optional<ExplanationIR> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            log.info("Cache hit for key: {}", cacheKey);
            return cached.get();
        }

        log.info("Cache miss for key: {}", cacheKey);

        // 1. Generate Reasoning Proposal
        ReasoningProposal proposal = reasoningService.generateReasoning(question);

        // 2. Validate Proposal
        ValidationResult validationResult = validationService.validateProposal(question, proposal);
        if (validationResult.getStatus() == ValidationStatus.FAILED) {
            throw new ValidationFailedException("Explanation proposal validation failed: " + validationResult.getViolations());
        }

        // 3. Compile to ExplanationIR
        ExplanationIR ir = compilationService.compileExplanation(question, proposal, policy);
        ir.setStatus(ExplanationStatus.APPROVED);

        // 4. Persist
        UUID explanationUuid = UUID.randomUUID();
        int nextVersion = 1;
        Optional<ExplanationEntity> latest = explanationRepository.findFirstByQuestionIdOrderByVersionDesc(question.getQuestionId());
        if (latest.isPresent() && latest.get().getVersion() != null) {
            nextVersion = latest.get().getVersion() + 1;
        }

        try {
            String contentJson = objectMapper.writeValueAsString(ir);
            ExplanationEntity entity = ExplanationEntity.builder()
                    .id(explanationUuid)
                    .questionId(question.getQuestionId())
                    .version(nextVersion)
                    .status(ExplanationStatus.APPROVED.name())
                    .difficulty(question.getDifficulty() != null ? question.getDifficulty() : "MEDIUM")
                    .language("en")
                    .schemaVersion("1.0")
                    .contentJson(contentJson)
                    .createdBy("system")
                    .createdAt(Instant.now())
                    .build();

            ExplanationEntity saved = explanationRepository.saveAndFlush(entity);
            log.info("Saved explanation entity: id={}, questionId={}, version={}", saved.getId(), saved.getQuestionId(), saved.getVersion());

            if (ir.getSteps() != null) {
                for (ReasoningStep step : ir.getSteps()) {
                    ExplanationStepEntity stepEntity = ExplanationStepEntity.builder()
                            .id(UUID.randomUUID())
                            .explanationId(explanationUuid)
                            .sequence(step.getSequence())
                            .stepType(step.getType().name())
                            .contentJson(objectMapper.writeValueAsString(step))
                            .validationStatus("VALID")
                            .build();
                    stepRepository.save(stepEntity);
                }
                stepRepository.flush();
            }

            validationService.recordValidationResult(explanationUuid, validationResult);
        } catch (Exception e) {
            log.error("Persistence error during explanation generation: ", e);
        }

        // 5. Cache
        cacheService.put(cacheKey, ir);

        return ir;
    }

    public ExplanationIR getExplanationByQuestionId(String questionId) {
        log.info("Finding explanation for questionId={}", questionId);
        Optional<ExplanationEntity> entityOpt = explanationRepository.findFirstByQuestionIdOrderByVersionDesc(questionId);
        if (entityOpt.isEmpty()) {
            log.warn("No entity found for questionId={}. Total entities in repo={}", questionId, explanationRepository.count());
            throw new ExplanationNotFoundException("Explanation not found for question ID: " + questionId);
        }

        try {
            return objectMapper.readValue(entityOpt.get().getContentJson(), ExplanationIR.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize explanation IR", e);
        }
    }

    public ReasoningStep getStep(UUID explanationId, String stepId) {
        Optional<ExplanationEntity> expOpt = explanationRepository.findById(explanationId);
        if (expOpt.isEmpty()) {
            throw new ExplanationNotFoundException("Explanation not found for ID: " + explanationId);
        }

        try {
            ExplanationIR ir = objectMapper.readValue(expOpt.get().getContentJson(), ExplanationIR.class);
            if (ir.getSteps() != null) {
                for (ReasoningStep step : ir.getSteps()) {
                    if (stepId.equalsIgnoreCase(step.getId()) || String.valueOf(step.getSequence()).equals(stepId)) {
                        return step;
                    }
                }
            }
            throw new ExplanationNotFoundException("Step " + stepId + " not found in explanation " + explanationId);
        } catch (ExplanationNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load step " + stepId, e);
        }
    }

    @Transactional
    public ExplanationIR reviewExplanation(UUID explanationId, boolean approved, String comment) {
        ExplanationEntity entity = explanationRepository.findById(explanationId)
                .orElseThrow(() -> new ExplanationNotFoundException("Explanation not found: " + explanationId));

        ExplanationStatus newStatus = approved ? ExplanationStatus.APPROVED : ExplanationStatus.REJECTED;
        entity.setStatus(newStatus.name());
        entity.setUpdatedAt(Instant.now());

        try {
            ExplanationIR ir = objectMapper.readValue(entity.getContentJson(), ExplanationIR.class);
            ir.setStatus(newStatus);
            entity.setContentJson(objectMapper.writeValueAsString(ir));
            explanationRepository.saveAndFlush(entity);
            return ir;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update explanation review status", e);
        }
    }

    @Transactional
    public ExplanationIR publishExplanation(UUID explanationId) {
        ExplanationEntity entity = explanationRepository.findById(explanationId)
                .orElseThrow(() -> new ExplanationNotFoundException("Explanation not found: " + explanationId));

        entity.setStatus(ExplanationStatus.PUBLISHED.name());
        entity.setPublishedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        try {
            ExplanationIR ir = objectMapper.readValue(entity.getContentJson(), ExplanationIR.class);
            ir.setStatus(ExplanationStatus.PUBLISHED);
            entity.setContentJson(objectMapper.writeValueAsString(ir));
            explanationRepository.saveAndFlush(entity);
            return ir;
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish explanation", e);
        }
    }

    @Transactional
    public void submitFeedback(UUID explanationId, UUID stepId, String userId, String feedbackType, String comment) {
        ExplanationFeedbackEntity feedback = ExplanationFeedbackEntity.builder()
                .id(UUID.randomUUID())
                .explanationId(explanationId)
                .stepId(stepId)
                .userId(userId)
                .feedbackType(feedbackType)
                .comment(comment)
                .createdAt(Instant.now())
                .build();
        feedbackRepository.save(feedback);
    }
}
