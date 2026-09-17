package ai.anvaya.prajna.cache;

import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.StudentLevel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExplanationCacheServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExplanationCacheService cacheService = new ExplanationCacheService(objectMapper, null);

    @Test
    void shouldComputeConsistentCacheKeys() {
        ExplanationPolicy policy = ExplanationPolicy.builder().level(StudentLevel.BEGINNER).animation("SMOOTH").build();
        String key = cacheService.computeCacheKey("q1", policy);
        assertThat(key).isEqualTo("explanation:q1:BEGINNER:SMOOTH");
    }

    @Test
    void shouldStoreAndRetrieveFromLocalCache() {
        ExplanationIR ir = ExplanationIR.builder().questionId("q1").explanationId("e1").build();
        String key = "test-key";

        cacheService.put(key, ir);
        Optional<ExplanationIR> cached = cacheService.get(key);

        assertThat(cached).isPresent();
        assertThat(cached.get().getExplanationId()).isEqualTo("e1");

        cacheService.evict(key);
        assertThat(cacheService.get(key)).isEmpty();
    }
}
