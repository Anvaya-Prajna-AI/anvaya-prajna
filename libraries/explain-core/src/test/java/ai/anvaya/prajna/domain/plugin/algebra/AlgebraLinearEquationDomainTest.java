package ai.anvaya.prajna.domain.plugin.algebra;

import ai.anvaya.prajna.domain.plugin.ExplanationDomain;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlgebraLinearEquationDomainTest {

    private final ExplanationDomain domain = new AlgebraLinearEquationDomain();

    @Test
    void shouldSupportAlgebraAndLinearEquations() {
        Question q1 = Question.builder().statement("Solve 3x + 5 = 20").domain("ALGEBRA").build();
        Question q2 = Question.builder().statement("Find x in 2x - 4 = 10").domain("MATH").build();

        assertThat(domain.supports(q1)).isTrue();
        assertThat(domain.supports(q2)).isTrue();
    }

    @Test
    void shouldGenerateProposalForLinearEquation() {
        Question q = Question.builder().statement("Solve 3x + 5 = 20").authoritativeAnswer("5").build();
        ReasoningProposal proposal = domain.generateReasoning(q);

        assertThat(proposal).isNotNull();
        assertThat(proposal.getSteps()).isNotEmpty();
        assertThat(proposal.getConclusion()).contains("5");
    }
}
