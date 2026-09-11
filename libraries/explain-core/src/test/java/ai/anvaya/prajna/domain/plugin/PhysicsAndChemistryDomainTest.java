package ai.anvaya.prajna.domain.plugin;

import ai.anvaya.prajna.application.CompilationService;
import ai.anvaya.prajna.domain.plugin.chemistry.ChemistryStoichiometryDomain;
import ai.anvaya.prajna.domain.plugin.physics.PhysicsMechanicsDomain;
import ai.anvaya.prajna.ir.DiagramIR;
import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhysicsAndChemistryDomainTest {

    private PhysicsMechanicsDomain physicsDomain;
    private ChemistryStoichiometryDomain chemistryDomain;
    private CompilationService compilationService;

    @BeforeEach
    void setUp() {
        physicsDomain = new PhysicsMechanicsDomain();
        chemistryDomain = new ChemistryStoichiometryDomain();
        compilationService = new CompilationService();
    }

    @Test
    @DisplayName("PhysicsDomain: Should support classical mechanics and Newton's Second Law")
    void testPhysicsDomainSupportAndSolving() {
        Question q = Question.builder()
                .questionId("q-phys-01")
                .domain("PHYSICS")
                .statement("A 10 kg block is pulled with a force of 50 N on a frictionless surface. What is its acceleration?")
                .authoritativeAnswer("5")
                .build();

        assertThat(physicsDomain.supports(q)).isTrue();
        assertThat(physicsDomain.getDomainName()).isEqualTo("PHYSICS");

        ReasoningProposal proposal = physicsDomain.generateReasoning(q);
        assertThat(proposal).isNotNull();
        assertThat(proposal.getSteps()).isNotEmpty();
        assertThat(proposal.getConclusion()).contains("5 m/s^2");
        assertThat(proposal.getVerification().getPassed()).isTrue();

        // Check Free Body Diagram
        assertThat(proposal.getDiagrams()).isNotEmpty();
        DiagramIR fbd = proposal.getDiagrams().get(0);
        assertThat(fbd.getType()).isEqualTo("free-body");
        assertThat(fbd.getTitle()).contains("Free-Body Diagram");
        assertThat(fbd.getObjects()).extracting("id").contains("body", "ground");
        assertThat(fbd.getRelationships()).extracting("to").contains("Fn", "Fg", "Fapp");

        // Check Misconceptions
        assertThat(proposal.getMisconceptions()).isNotEmpty();
        assertThat(proposal.getMisconceptions().get(0).getIncorrectReasoning()).contains("continuous forward force");

        // Test End-to-End Compilation to ExplanationIR
        ExplanationIR ir = compilationService.compileExplanation(q, proposal, ExplanationPolicy.builder().showWhy(true).build());
        assertThat(ir).isNotNull();
        assertThat(ir.getDiagrams()).isNotEmpty();
        assertThat(ir.getDiagrams().get(0).getType()).isEqualTo("free-body");
        assertThat(ir.getReasoningGraph().getNodes()).isNotEmpty();
    }

    @Test
    @DisplayName("ChemistryDomain: Should support stoichiometry, equation balancing, and reaction energy profile")
    void testChemistryDomainSupportAndSolving() {
        Question q = Question.builder()
                .questionId("q-chem-01")
                .domain("CHEMISTRY")
                .statement("Balance the reaction: H2 + O2 -> H2O. How many grams of H2O are produced from 4 g of H2 reacting with excess oxygen?")
                .authoritativeAnswer("35.7")
                .build();

        assertThat(chemistryDomain.supports(q)).isTrue();
        assertThat(chemistryDomain.getDomainName()).isEqualTo("CHEMISTRY");

        ReasoningProposal proposal = chemistryDomain.generateReasoning(q);
        assertThat(proposal).isNotNull();
        assertThat(proposal.getSteps()).isNotEmpty();
        assertThat(proposal.getConclusion()).contains("Balanced: 2 H_2 + O_2 -> 2 H_2O");
        assertThat(proposal.getVerification().getPassed()).isTrue();

        // Check Reaction Energy Diagram
        assertThat(proposal.getDiagrams()).isNotEmpty();
        DiagramIR energyDiag = proposal.getDiagrams().get(0);
        assertThat(energyDiag.getType()).isEqualTo("reaction-energy");
        assertThat(energyDiag.getObjects()).extracting("id").contains("reactants", "transition_state", "products");
        assertThat(energyDiag.getRelationships()).extracting("type").contains("ACTIVATION_ENERGY", "ENTHALPY_CHANGE");

        // Check Steps contain GIVEN, TRANSFORM (balancing), and CALCULATE (moles/yield)
        List<StepType> stepTypes = proposal.getSteps().stream().map(ReasoningStep::getType).toList();
        assertThat(stepTypes).contains(StepType.GIVEN, StepType.TRANSFORM, StepType.CALCULATE, StepType.VERIFY);

        // Check Misconceptions
        assertThat(proposal.getMisconceptions()).isNotEmpty();
        assertThat(proposal.getMisconceptions().get(0).getWhyWrong()).contains("hydrogen peroxide");

        // Test End-to-End Compilation to ExplanationIR
        ExplanationIR ir = compilationService.compileExplanation(q, proposal, ExplanationPolicy.builder().showWhy(true).build());
        assertThat(ir).isNotNull();
        assertThat(ir.getDiagrams().get(0).getType()).isEqualTo("reaction-energy");
    }
}
