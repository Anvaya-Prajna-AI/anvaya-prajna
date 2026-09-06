package ai.anvaya.prajna.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ModelRouter {

    @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}")
    private String defaultModel;

    public String routeModel(String domain, String difficulty) {
        if ("HARD".equalsIgnoreCase(difficulty) || "ADVANCED".equalsIgnoreCase(difficulty)) {
            return "gpt-4o";
        }
        return defaultModel;
    }
}
