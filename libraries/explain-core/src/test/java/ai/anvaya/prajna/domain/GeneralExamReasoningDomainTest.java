package ai.anvaya.prajna.domain;

import ai.anvaya.prajna.domain.plugin.ExplanationDomainRegistry;
import ai.anvaya.prajna.domain.plugin.algebra.AlgebraLinearEquationDomain;
import ai.anvaya.prajna.domain.plugin.general.GeneralExamReasoningDomain;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import ai.anvaya.prajna.validation.ValidationResult;
import ai.anvaya.prajna.validation.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeneralExamReasoningDomainTest {

    private GeneralExamReasoningDomain generalDomain;
    private AlgebraLinearEquationDomain algebraDomain;
    private ExplanationDomainRegistry registry;

    @BeforeEach
    void setUp() {
        generalDomain = new GeneralExamReasoningDomain();
        algebraDomain = new AlgebraLinearEquationDomain();
        registry = new ExplanationDomainRegistry(List.of(generalDomain, algebraDomain));
    }

    @Test
    void shouldSupportGeneralCompetitiveExamQuestions() {
        Question q = Question.builder()
                .questionId("q-upsc-1")
                .statement("What is the primary function of the Reserve Bank of India's Monetary Policy Committee?")
                .domain("ECONOMICS")
                .authoritativeAnswer("Fixing the benchmark policy repo rate to maintain price stability while keeping in mind growth.")
                .build();

        assertThat(generalDomain.supports(q)).isTrue();
        assertThat(generalDomain.getDomainName()).isEqualTo("GENERAL_EXAM");
    }

    @Test
    void shouldRejectEmptyQuestion() {
        Question empty = Question.builder().statement("   ").build();
        assertThat(generalDomain.supports(empty)).isFalse();
        assertThat(generalDomain.supports(null)).isFalse();
    }

    @Test
    void shouldGenerateStructuredReasoningProposal() {
        Question q = Question.builder()
                .questionId("q-gate-cs-1")
                .statement("In a B+ tree of order d, what is the maximum number of keys in a leaf node?")
                .domain("COMPUTER_SCIENCE")
                .authoritativeAnswer("2d")
                .build();

        ReasoningProposal proposal = generalDomain.generateReasoning(q);

        assertThat(proposal).isNotNull();
        assertThat(proposal.getQuestionId()).isEqualTo("q-gate-cs-1");
        assertThat(proposal.getSteps()).hasSize(4);
        assertThat(proposal.getConclusion()).isEqualTo("2d");
        assertThat(proposal.getHints()).hasSize(3);
        assertThat(proposal.getMisconceptions()).hasSize(1);
        assertThat(proposal.getVerification().getPassed()).isTrue();

        ValidationResult val = generalDomain.validate(proposal);
        assertThat(val.getStatus()).isEqualTo(ValidationStatus.PASSED);
    }

    @Test
    void shouldPrioritizeSpecificDomainOverFallbackInRegistry() {
        Question algebraQ = Question.builder()
                .questionId("q-alg-1")
                .statement("Solve for x: 2x + 4 = 12")
                .domain("ALGEBRA")
                .authoritativeAnswer("4")
                .build();

        var resolved = registry.findDomain(algebraQ);
        assertThat(resolved).isPresent();
        assertThat(resolved.get().getDomainName()).isEqualTo("ALGEBRA");
    }

    @Test
    void shouldFallbackToGeneralExamWhenNoSpecificDomainMatches() {
        Question generalQ = Question.builder()
                .questionId("q-his-1")
                .statement("Explain the significance of the 1905 Swadeshi Movement in India.")
                .domain("HISTORY")
                .authoritativeAnswer("Spurred indigenous manufacturing and boycott of foreign textiles.")
                .build();

        var resolved = registry.findDomain(generalQ);
        assertThat(resolved).isPresent();
        assertThat(resolved.get().getDomainName()).isEqualTo("GENERAL_EXAM");
    }
}
