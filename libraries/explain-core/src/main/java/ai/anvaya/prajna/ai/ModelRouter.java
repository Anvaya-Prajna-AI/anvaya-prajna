package ai.anvaya.prajna.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Intelligent Model Router for AWS Bedrock Nova and Google Gemini model suites.
 * Routes dynamically based on subject domain, difficulty level, and latency requirements.
 * Provides resilient fallback hierarchies across model families.
 */
@Component
public class ModelRouter {

    private static final Logger log = LoggerFactory.getLogger(ModelRouter.class);

    // Amazon Bedrock Nova Family
    public static final String NOVA_PRO = "amazon-nova-pro";
    public static final String NOVA_LITE = "amazon-nova-lite";
    public static final String NOVA_MICRO = "amazon-nova-micro";

    // Google Gemini Family
    public static final String GEMINI_PRO = "gemini-1.5-pro";
    public static final String GEMINI_FLASH = "gemini-1.5-flash";
    public static final String GEMINI_2_FLASH = "gemini-2.0-flash";

    @Value("${anvaya.ai.target-suite:auto}")
    private String targetSuite; // "bedrock", "gemini", or "auto"

    @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}")
    private String configuredDefaultModel;

    /**
     * Determines the optimal primary reasoning model based on domain and question complexity.
     */
    public String routeModel(String domain, String difficulty) {
        boolean isHighComplexity = "HARD".equalsIgnoreCase(difficulty) ||
                "ADVANCED".equalsIgnoreCase(difficulty) ||
                "COLLEGE".equalsIgnoreCase(difficulty) ||
                "CALCULUS".equalsIgnoreCase(domain) ||
                "PHYSICS".equalsIgnoreCase(domain);

        boolean isLowLatencyTask = "HINT".equalsIgnoreCase(domain) || "WHY".equalsIgnoreCase(domain);

        if ("bedrock".equalsIgnoreCase(targetSuite)) {
            if (isLowLatencyTask) return NOVA_MICRO;
            return isHighComplexity ? NOVA_PRO : NOVA_LITE;
        }

        if ("gemini".equalsIgnoreCase(targetSuite)) {
            if (isLowLatencyTask) return GEMINI_FLASH;
            return isHighComplexity ? GEMINI_PRO : GEMINI_FLASH;
        }

        // Auto routing: Nova Pro for complex reasoning, Gemini Flash for fast standard explanations
        if (isHighComplexity) {
            return NOVA_PRO;
        }

        // If a specific default model is set in config (e.g. gpt-4o-mini or gemini-1.5-flash)
        if (configuredDefaultModel != null && !configuredDefaultModel.isBlank()) {
            return configuredDefaultModel;
        }

        return NOVA_LITE;
    }

    /**
     * Provides an automated fallback model from the alternate family if the primary model fails.
     */
    public String getFallbackModel(String primaryModel) {
        if (primaryModel == null) return GEMINI_FLASH;

        switch (primaryModel.toLowerCase()) {
            case NOVA_PRO:
            case "amazon.nova-pro-v1:0":
                return GEMINI_PRO;
            case NOVA_LITE:
            case "amazon.nova-lite-v1:0":
                return GEMINI_FLASH;
            case NOVA_MICRO:
            case "amazon.nova-micro-v1:0":
                return GEMINI_FLASH;
            case GEMINI_PRO:
                return NOVA_PRO;
            case GEMINI_FLASH:
            case GEMINI_2_FLASH:
                return NOVA_LITE;
            case "gpt-4o":
                return NOVA_PRO;
            default:
                return GEMINI_FLASH;
        }
    }

    public boolean isBedrockNovaModel(String model) {
        return model != null && (model.contains("nova") || model.startsWith("amazon.nova"));
    }

    public boolean isGeminiModel(String model) {
        return model != null && model.contains("gemini");
    }
}
