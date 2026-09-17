package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepJustification;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.ir.StudentLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PedagogicalPlannerTest {

    private final PedagogicalPlanner planner = new PedagogicalPlanner();

    @Test
    void shouldReturnEmptyForNullOrEmptyInput() {
        assertThat(planner.planSteps(null, null)).isEmpty();
        assertThat(planner.planSteps(List.of(), null)).isEmpty();
    }

    @Test
    void shouldFilterFormulasWhenShowFormulaIsFalse() {
        ReasoningStep ruleStep = ReasoningStep.builder().id("s1").type(StepType.APPLY_RULE).build();
        ReasoningStep inferStep = ReasoningStep.builder().id("s2").type(StepType.INFER).build();

        ExplanationPolicy policy = ExplanationPolicy.builder().showFormula(false).build();
        List<ReasoningStep> result = planner.planSteps(List.of(ruleStep, inferStep), policy);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("s2");
        assertThat(result.get(0).getSequence()).isEqualTo(1);
    }

    @Test
    void shouldFilterCalculationsForAdvancedLevelWhenShowEveryCalculationIsFalse() {
        ReasoningStep calcStep = ReasoningStep.builder().id("s1").type(StepType.CALCULATE).build();
        ExplanationPolicy policy = ExplanationPolicy.builder()
                .level(StudentLevel.ADVANCED)
                .showEveryCalculation(false)
                .build();

        List<ReasoningStep> result = planner.planSteps(List.of(calcStep), policy);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldAttachJustificationWhenShowWhyIsTrue() {
        StepJustification justification = StepJustification.builder().text("Because of balance law").build();
        ReasoningStep step = ReasoningStep.builder().id("s1").type(StepType.TRANSFORM).justification(justification).build();

        ExplanationPolicy policyWithWhy = ExplanationPolicy.builder().showWhy(true).build();
        List<ReasoningStep> withWhy = planner.planSteps(List.of(step), policyWithWhy);
        assertThat(withWhy.get(0).getJustification()).isNotNull();

        ExplanationPolicy policyWithoutWhy = ExplanationPolicy.builder().showWhy(false).build();
        List<ReasoningStep> withoutWhy = planner.planSteps(List.of(step), policyWithoutWhy);
        assertThat(withoutWhy.get(0).getJustification()).isNull();
    }
}
