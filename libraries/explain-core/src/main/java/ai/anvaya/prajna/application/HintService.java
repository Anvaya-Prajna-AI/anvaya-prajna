package ai.anvaya.prajna.application;

import ai.anvaya.prajna.cache.ExplanationCacheService;
import ai.anvaya.prajna.domain.entity.ExplanationEntity;
import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.Hint;
import ai.anvaya.prajna.repository.ExplanationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HintService {

    private final ExplanationRepository explanationRepository;
    private final ExplanationCacheService cacheService;
    private final ObjectMapper objectMapper;

    public HintService(ExplanationRepository explanationRepository,
                       ExplanationCacheService cacheService,
                       ObjectMapper objectMapper) {
        this.explanationRepository = explanationRepository;
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
    }

    public Optional<Hint> getHint(String questionId, int level) {
        Optional<ExplanationEntity> entityOpt = explanationRepository.findFirstByQuestionIdOrderByVersionDesc(questionId);
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        try {
            ExplanationIR ir = objectMapper.readValue(entityOpt.get().getContentJson(), ExplanationIR.class);
            List<Hint> hints = ir.getHints();
            if (hints == null || hints.isEmpty()) {
                return Optional.of(Hint.builder()
                        .id("h" + level)
                        .level(level)
                        .text("Review the problem statement and identify the known quantities.")
                        .build());
            }

            return hints.stream()
                    .filter(h -> h.getLevel() != null && h.getLevel() == level)
                    .findFirst()
                    .or(() -> Optional.of(hints.get(Math.min(level - 1, hints.size() - 1))));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
