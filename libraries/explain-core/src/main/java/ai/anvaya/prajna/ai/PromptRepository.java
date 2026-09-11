package ai.anvaya.prajna.ai;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PromptRepository {

    private final ResourceLoader resourceLoader;
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public PromptRepository(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String getPrompt(String promptName) {
        return cache.computeIfAbsent(promptName, name -> {
            try {
                Resource res = resourceLoader.getResource("classpath:prompts/" + name);
                if (res.exists()) {
                    try (InputStream is = res.getInputStream()) {
                        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }
            } catch (Exception ignored) {}
            return getDefaultPrompt(name);
        });
    }

    private String getDefaultPrompt(String name) {
        if (name.contains("hint")) {
            return "You are an expert educational tutor. Given the following question and solution steps, generate a progressive hint at level {level} that helps the student think through the next step without revealing the full answer directly.";
        }
        if (name.contains("why")) {
            return "You are an expert pedagogical reasoning engine. Explain the deep mathematical or logical justification for the following reasoning step: '{step}'. Answer concisely why this step is mathematically valid and why it helps solve the problem.";
        }
        return "You are Anvaya-Prajna-AI, a pedagogical reasoning engine. Generate a structured step-by-step reasoning proposal for the given question in JSON format.";
    }
}
