package ai.anvaya.prajna.domain.plugin;

import ai.anvaya.prajna.domain.plugin.algebra.AlgebraLinearEquationDomain;
import ai.anvaya.prajna.domain.plugin.arithmetic.ArithmeticDomain;
import ai.anvaya.prajna.domain.plugin.logic.SyllogismDomain;
import ai.anvaya.prajna.domain.plugin.numberseries.NumberSeriesDomain;
import ai.anvaya.prajna.domain.plugin.ratio.PercentageRatioDomain;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainPluginsTest {

    @Test
    void shouldHandleArithmeticDomain() {
        ArithmeticDomain domain = new ArithmeticDomain();
        Question q = Question.builder().questionId("q1").statement("12 * 5 + 10").domain("ARITHMETIC").build();
        assertThat(domain.supports(q)).isTrue();

        ReasoningProposal proposal = domain.generateReasoning(q);
        assertThat(proposal.getConclusion()).isEqualTo("70");
        assertThat(proposal.getSteps()).hasSize(3);
    }

    @Test
    void shouldHandleNumberSeriesDomain() {
        NumberSeriesDomain domain = new NumberSeriesDomain();
        Question q = Question.builder().questionId("q2").statement("2, 4, 6, 8, ?").domain("NUMBER_SERIES").build();
        assertThat(domain.supports(q)).isTrue();

        ReasoningProposal proposal = domain.generateReasoning(q);
        assertThat(proposal.getConclusion()).contains("10");
        assertThat(proposal.getSteps()).hasSize(3);
    }

    @Test
    void shouldHandleSyllogismDomain() {
        SyllogismDomain domain = new SyllogismDomain();
        Question q = Question.builder().questionId("q3").statement("A is taller than B. B is taller than C.").domain("LOGICAL_REASONING").build();
        assertThat(domain.supports(q)).isTrue();

        ReasoningProposal proposal = domain.generateReasoning(q);
        assertThat(proposal.getConclusion()).contains("shortest");
        assertThat(proposal.getDiagrams()).isNotEmpty();
    }

    @Test
    void shouldHandleRegistryLookup() {
        ExplanationDomainRegistry registry = new ExplanationDomainRegistry(List.of(
                new AlgebraLinearEquationDomain(),
                new ArithmeticDomain(),
                new NumberSeriesDomain(),
                new SyllogismDomain(),
                new PercentageRatioDomain()
        ));

        Question qAlg = Question.builder().statement("3x + 5 = 20").build();
        assertThat(registry.findDomain(qAlg)).isPresent();
        assertThat(registry.findDomain(qAlg).get().getDomainName()).isEqualTo("ALGEBRA");
    }

    @Test
    void shouldHandleDynamicSyllogismEntities() {
        SyllogismDomain domain = new SyllogismDomain();
        Question q = Question.builder()
                .questionId("q-syl-dyn")
                .statement("John is taller than Alice. Alice is taller than Bob. Who is the tallest?")
                .domain("LOGICAL_REASONING")
                .build();
        assertThat(domain.supports(q)).isTrue();

        ReasoningProposal proposal = domain.generateReasoning(q);
        assertThat(proposal.getConclusion()).contains("John is the tallest");
        assertThat(proposal.getFacts()).contains("John > Alice", "Alice > Bob");
        assertThat(proposal.getDiagrams()).isNotEmpty();
    }

    @Test
    void shouldHandleCategoricalSyllogism() {
        SyllogismDomain domain = new SyllogismDomain();
        Question q = Question.builder()
                .questionId("q-syl-cat")
                .statement("All cats are mammals. All mammals are animals.")
                .domain("LOGICAL_REASONING")
                .build();
        assertThat(domain.supports(q)).isTrue();

        ReasoningProposal proposal = domain.generateReasoning(q);
        assertThat(proposal.getConclusion()).isEqualTo("All cats are animals");
        assertThat(proposal.getSteps()).hasSize(3);
    }

    @Test
    void shouldHandlePercentageCalculation() {
        PercentageRatioDomain domain = new PercentageRatioDomain();
        Question q = Question.builder()
                .questionId("q-pct")
                .statement("What is 20% of 150?")
                .domain("PERCENTAGE")
                .build();
        assertThat(domain.supports(q)).isTrue();

        ReasoningProposal proposal = domain.generateReasoning(q);
        assertThat(proposal.getConclusion()).contains("30");
        assertThat(proposal.getVerification().getPassed()).isTrue();
    }

    @Test
    void shouldHandleRatioDistribution() {
        PercentageRatioDomain domain = new PercentageRatioDomain();
        Question q = Question.builder()
                .questionId("q-ratio")
                .statement("Divide 100 in the ratio 2:3")
                .domain("RATIO")
                .build();
        assertThat(domain.supports(q)).isTrue();

        ReasoningProposal proposal = domain.generateReasoning(q);
        assertThat(proposal.getConclusion()).contains("40");
        assertThat(proposal.getConclusion()).contains("60");
        assertThat(proposal.getVerification().getPassed()).isTrue();
    }

    @Test
    void shouldHandleGeometricProgression() {
        NumberSeriesDomain domain = new NumberSeriesDomain();
        Question q = Question.builder()
                .questionId("q-gp")
                .statement("3, 6, 12, 24, ?")
                .domain("NUMBER_SERIES")
                .build();
        assertThat(domain.supports(q)).isTrue();

        ReasoningProposal proposal = domain.generateReasoning(q);
        assertThat(proposal.getConclusion()).contains("48");
        assertThat(proposal.getConcepts()).contains("geometric-progression");
    }

    @Test
    void shouldHandleFibonacciSequence() {
        NumberSeriesDomain domain = new NumberSeriesDomain();
        Question q = Question.builder()
                .questionId("q-fib")
                .statement("1, 2, 3, 5, 8, ?")
                .domain("NUMBER_SERIES")
                .build();
        assertThat(domain.supports(q)).isTrue();

        ReasoningProposal proposal = domain.generateReasoning(q);
        assertThat(proposal.getConclusion()).contains("13");
        assertThat(proposal.getConcepts()).contains("fibonacci-sequence");
    }
}
