package ai.anvaya.prajna.cache;

import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.ExplanationPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExplanationCacheService {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final Map<String, String> localMemoryCache = new ConcurrentHashMap<>();

    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    @Autowired
    public ExplanationCacheService(ObjectMapper objectMapper,
                                  @Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public String computeCacheKey(String questionId, ExplanationPolicy policy) {
        String level = policy != null && policy.getLevel() != null ? policy.getLevel().name() : "DEFAULT";
        String anim = policy != null && policy.getAnimation() != null ? policy.getAnimation() : "DEFAULT";
        return "explanation:" + questionId + ":" + level + ":" + anim;
    }

    public Optional<ExplanationIR> get(String key) {
        try {
            if (redisTemplate != null) {
                String json = redisTemplate.opsForValue().get(key);
                if (json != null) {
                    return Optional.of(objectMapper.readValue(json, ExplanationIR.class));
                }
            } else {
                String json = localMemoryCache.get(key);
                if (json != null) {
                    return Optional.of(objectMapper.readValue(json, ExplanationIR.class));
                }
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    public void put(String key, ExplanationIR explanation) {
        try {
            String json = objectMapper.writeValueAsString(explanation);
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, json, DEFAULT_TTL);
            } else {
                localMemoryCache.put(key, json);
            }
        } catch (Exception ignored) {}
    }

    public void evict(String key) {
        try {
            if (redisTemplate != null) {
                redisTemplate.delete(key);
            } else {
                localMemoryCache.remove(key);
            }
        } catch (Exception ignored) {}
    }
}
