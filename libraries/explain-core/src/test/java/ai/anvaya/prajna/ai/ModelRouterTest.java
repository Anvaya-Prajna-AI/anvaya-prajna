package ai.anvaya.prajna.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ModelRouterTest {

    private ModelRouter modelRouter;

    @BeforeEach
    void setUp() {
        modelRouter = new ModelRouter();
    }

    @Test
    void shouldRouteToNovaProForAdvancedOrHardDifficultyInAutoMode() {
        ReflectionTestUtils.setField(modelRouter, "targetSuite", "auto");

        String model = modelRouter.routeModel("ALGEBRA", "HARD");
        assertThat(model).isEqualTo(ModelRouter.NOVA_PRO);

        String physicsModel = modelRouter.routeModel("PHYSICS", "MEDIUM");
        assertThat(physicsModel).isEqualTo(ModelRouter.NOVA_PRO);
    }

    @Test
    void shouldRouteToNovaLiteOrConfiguredDefaultForStandardDifficulty() {
        ReflectionTestUtils.setField(modelRouter, "targetSuite", "auto");
        ReflectionTestUtils.setField(modelRouter, "configuredDefaultModel", "gpt-4o-mini");

        String model = modelRouter.routeModel("ARITHMETIC", "EASY");
        assertThat(model).isEqualTo("gpt-4o-mini");
    }

    @Test
    void shouldSupportBedrockSuiteExplicitly() {
        ReflectionTestUtils.setField(modelRouter, "targetSuite", "bedrock");

        String hardModel = modelRouter.routeModel("ALGEBRA", "HARD");
        assertThat(hardModel).isEqualTo(ModelRouter.NOVA_PRO);

        String standardModel = modelRouter.routeModel("ARITHMETIC", "MEDIUM");
        assertThat(standardModel).isEqualTo(ModelRouter.NOVA_LITE);

        String hintModel = modelRouter.routeModel("HINT", "MEDIUM");
        assertThat(hintModel).isEqualTo(ModelRouter.NOVA_MICRO);
    }

    @Test
    void shouldSupportGeminiSuiteExplicitly() {
        ReflectionTestUtils.setField(modelRouter, "targetSuite", "gemini");

        String hardModel = modelRouter.routeModel("ALGEBRA", "ADVANCED");
        assertThat(hardModel).isEqualTo(ModelRouter.GEMINI_PRO);

        String standardModel = modelRouter.routeModel("ARITHMETIC", "EASY");
        assertThat(standardModel).isEqualTo(ModelRouter.GEMINI_FLASH);

        String whyModel = modelRouter.routeModel("WHY", "EASY");
        assertThat(whyModel).isEqualTo(ModelRouter.GEMINI_FLASH);
    }

    @Test
    void shouldProvideResilientFallbacksAcrossFamilies() {
        // Nova Pro falls back to Gemini Pro
        assertThat(modelRouter.getFallbackModel(ModelRouter.NOVA_PRO)).isEqualTo(ModelRouter.GEMINI_PRO);

        // Nova Lite falls back to Gemini Flash
        assertThat(modelRouter.getFallbackModel(ModelRouter.NOVA_LITE)).isEqualTo(ModelRouter.GEMINI_FLASH);

        // Gemini Pro falls back to Nova Pro
        assertThat(modelRouter.getFallbackModel(ModelRouter.GEMINI_PRO)).isEqualTo(ModelRouter.NOVA_PRO);

        // Gemini Flash falls back to Nova Lite
        assertThat(modelRouter.getFallbackModel(ModelRouter.GEMINI_FLASH)).isEqualTo(ModelRouter.NOVA_LITE);
    }
}
