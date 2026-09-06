package ai.anvaya.prajna.api;

import ai.anvaya.prajna.api.dto.FeedbackRequest;
import ai.anvaya.prajna.api.dto.GenerateExplanationRequest;
import ai.anvaya.prajna.api.dto.ValidateProposalRequest;
import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepOperation;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.ir.StudentLevel;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExplanationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGenerateAlgebraExplanationSuccessfully() throws Exception {
        Question question = Question.builder()
                .questionId("q-alg-101")
                .statement("Solve for x: 3x + 5 = 20")
                .domain("ALGEBRA")
                .authoritativeAnswer("5")
                .build();

        ExplanationPolicy policy = ExplanationPolicy.builder()
                .level(StudentLevel.INTERMEDIATE)
                .showWhy(true)
                .showVerification(true)
                .animation("LIGHT")
                .build();

        GenerateExplanationRequest request = GenerateExplanationRequest.builder()
                .question(question)
                .policy(policy)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/explanations/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value("q-alg-101"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.steps").isArray())
                .andExpect(jsonPath("$.reasoningGraph").exists())
                .andExpect(jsonPath("$.animation").exists())
                .andReturn();

        ExplanationIR ir = objectMapper.readValue(result.getResponse().getContentAsString(), ExplanationIR.class);
        assertThat(ir.getSteps()).isNotEmpty();
        assertThat(ir.getVerification()).isNotNull();
        assertThat(ir.getVerification().getPassed()).isTrue();

        // Query by question ID
        mockMvc.perform(get("/api/v1/explanations/q-alg-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value("q-alg-101"));

        // Query progressive hint
        mockMvc.perform(get("/api/v1/explanations/q-alg-101/hints").param("level", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").isNotEmpty());

        // Query why for step 1
        mockMvc.perform(get("/api/v1/explanations/q-alg-101/why").param("stepId", "s1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").isNotEmpty());
    }

    @Test
    void shouldGenerateSpeedDistanceTimeExplanation() throws Exception {
        Question question = Question.builder()
                .questionId("q-speed-202")
                .statement("A car travels 120 km in 2 hours. What is its speed?")
                .domain("PERCENTAGE")
                .authoritativeAnswer("60")
                .build();

        GenerateExplanationRequest request = GenerateExplanationRequest.builder()
                .question(question)
                .build();

        mockMvc.perform(post("/api/v1/explanations/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value("q-speed-202"))
                .andExpect(jsonPath("$.diagrams").isArray())
                .andExpect(jsonPath("$.verification.passed").value(true));
    }

    @Test
    void shouldValidateProposalDirectly() throws Exception {
        ReasoningStep step = ReasoningStep.builder()
                .id("s1")
                .type(StepType.TRANSFORM)
                .operation(StepOperation.builder().name("SUBTRACT").target("both-sides").value(5).build())
                .before("3x + 5 = 20")
                .after("3x = 15")
                .build();

        ReasoningProposal proposal = ReasoningProposal.builder()
                .questionId("q-test-validate")
                .steps(List.of(step))
                .conclusion("3x = 15")
                .build();

        ValidateProposalRequest request = ValidateProposalRequest.builder()
                .proposal(proposal)
                .build();

        mockMvc.perform(post("/api/v1/explanations/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PASSED"));
    }

    @Test
    void shouldSubmitFeedback() throws Exception {
        UUID expId = UUID.randomUUID();
        FeedbackRequest feedback = FeedbackRequest.builder()
                .userId("student-42")
                .feedbackType("HELPFUL")
                .comment("Very clear step-by-step breakdown!")
                .build();

        mockMvc.perform(post("/api/v1/explanations/" + expId + "/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isOk());
    }
}
