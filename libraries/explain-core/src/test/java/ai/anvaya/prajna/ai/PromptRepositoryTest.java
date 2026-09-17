package ai.anvaya.prajna.ai;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class PromptRepositoryTest {

    private final PromptRepository repository = new PromptRepository(new DefaultResourceLoader());

    @Test
    void shouldReturnDefaultPromptsForVariousKeys() {
        String hintPrompt = repository.getPrompt("hint-template.txt");
        assertThat(hintPrompt).contains("progressive hint");

        String whyPrompt = repository.getPrompt("why-template.txt");
        assertThat(whyPrompt).contains("pedagogical reasoning engine");

        String genericPrompt = repository.getPrompt("system-plan.txt");
        assertThat(genericPrompt).contains("Anvaya-Prajna-AI");
    }
}
