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
import ai.anvaya.prajna.security.ExampleSecurityContextFilter;
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

    @Autowired
    private ai.anvaya.prajna.repository.ExplanationRepository explanationRepository;

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

    @Test
    void shouldSupportLifecycleAndStepEndpoints() throws Exception {
        Question question = Question.builder()
                .questionId("q-lifecycle-test")
                .statement("Solve for x: 2x + 4 = 10")
                .domain("ALGEBRA")
                .authoritativeAnswer("3")
                .build();

        GenerateExplanationRequest request = GenerateExplanationRequest.builder()
                .question(question)
                .build();

        // 1. Generate explanation
        mockMvc.perform(post("/api/v1/explanations/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value("q-lifecycle-test"));

        var entityOpt = explanationRepository.findFirstByQuestionIdOrderByVersionDesc("q-lifecycle-test");
        assertThat(entityOpt).isPresent();
        UUID entityId = entityOpt.get().getId();
        assertThat(entityOpt.get().getVersion()).isEqualTo(1);

        // 2. Query specific step
        mockMvc.perform(get("/api/v1/explanations/" + entityId + "/steps/s1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("s1"));

        // 3. Review explanation
        ai.anvaya.prajna.api.dto.ReviewExplanationRequest reviewRequest = ai.anvaya.prajna.api.dto.ReviewExplanationRequest.builder()
                .approved(true)
                .reviewerId("teacher-1")
                .comment("Approved for publication")
                .build();

        mockMvc.perform(post("/api/v1/explanations/" + entityId + "/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // 4. Publish explanation
        mockMvc.perform(post("/api/v1/explanations/" + entityId + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        // 5. Verify published entity in repository
        var updated = explanationRepository.findById(entityId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo("PUBLISHED");
        assertThat(updated.getPublishedAt()).isNotNull();
    }

    @Test
    void shouldEnforceAsi03RoleBasedAccessControl() throws Exception {
        Question question = Question.builder()
                .questionId("q-rbac-test")
                .statement("Solve for x: x + 1 = 2")
                .domain("ALGEBRA")
                .authoritativeAnswer("1")
                .build();

        GenerateExplanationRequest request = GenerateExplanationRequest.builder()
                .question(question)
                .build();

        mockMvc.perform(post("/api/v1/explanations/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var entityOpt = explanationRepository.findFirstByQuestionIdOrderByVersionDesc("q-rbac-test");
        assertThat(entityOpt).isPresent();
        UUID entityId = entityOpt.get().getId();

        // 1. Student attempting to review should be rejected with 403 Forbidden (ASI03 defense)
        ai.anvaya.prajna.api.dto.ReviewExplanationRequest reviewRequest = ai.anvaya.prajna.api.dto.ReviewExplanationRequest.builder()
                .approved(true)
                .reviewerId("student-malicious")
                .comment("Unauthorized review")
                .build();

        mockMvc.perform(post("/api/v1/explanations/" + entityId + "/review")
                        .header(ExampleSecurityContextFilter.HEADER_USER_ID, "student-1")
                        .header(ExampleSecurityContextFilter.HEADER_ROLES, "STUDENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden (ASI03 Identity & Privilege Abuse)"));

        // 2. Educator attempting to publish curriculum should be rejected with 403 Forbidden (Admin only)
        mockMvc.perform(post("/api/v1/explanations/" + entityId + "/publish")
                        .header(ExampleSecurityContextFilter.HEADER_USER_ID, "teacher-1")
                        .header(ExampleSecurityContextFilter.HEADER_ROLES, "EDUCATOR")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden (ASI03 Identity & Privilege Abuse)"));

        // 3. Admin attempting to publish should succeed
        mockMvc.perform(post("/api/v1/explanations/" + entityId + "/publish")
                        .header(ExampleSecurityContextFilter.HEADER_USER_ID, "admin-1")
                        .header(ExampleSecurityContextFilter.HEADER_ROLES, "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void shouldIncrementVersionOnSubsequentGenerations() throws Exception {
        Question question = Question.builder()
                .questionId("q-version-test")
                .statement("Solve for x: 3x + 3 = 12")
                .domain("ALGEBRA")
                .authoritativeAnswer("3")
                .build();

        GenerateExplanationRequest request = GenerateExplanationRequest.builder()
                .question(question)
                .build();

        // First generation -> version 1
        mockMvc.perform(post("/api/v1/explanations/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var v1Entity = explanationRepository.findFirstByQuestionIdOrderByVersionDesc("q-version-test");
        assertThat(v1Entity).isPresent();
        assertThat(v1Entity.get().getVersion()).isEqualTo(1);

        // Second generation with a different policy (to bypass cache or test increment)
        ExplanationPolicy diffPolicy = ExplanationPolicy.builder().showWhy(true).build();
        GenerateExplanationRequest requestV2 = GenerateExplanationRequest.builder()
                .question(question)
                .policy(diffPolicy)
                .build();

        mockMvc.perform(post("/api/v1/explanations/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestV2)))
                .andExpect(status().isOk());

        var v2Entity = explanationRepository.findFirstByQuestionIdOrderByVersionDesc("q-version-test");
        assertThat(v2Entity).isPresent();
        assertThat(v2Entity.get().getVersion()).isGreaterThan(1);
    }
}
