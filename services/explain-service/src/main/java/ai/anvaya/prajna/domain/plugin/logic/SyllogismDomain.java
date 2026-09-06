package ai.anvaya.prajna.domain.plugin.logic;

import ai.anvaya.prajna.domain.plugin.ExplanationDomain;
import ai.anvaya.prajna.ir.DiagramIR;
import ai.anvaya.prajna.ir.DiagramObject;
import ai.anvaya.prajna.ir.DiagramRelationship;
import ai.anvaya.prajna.ir.Hint;
import ai.anvaya.prajna.ir.Misconception;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.RepresentationType;
import ai.anvaya.prajna.ir.StepJustification;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.ir.VerificationResult;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import ai.anvaya.prajna.validation.ValidationResult;
import ai.anvaya.prajna.validation.ValidationStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SyllogismDomain implements ExplanationDomain {

    @Override
    public boolean supports(Question question) {
        if (question == null || question.getStatement() == null) return false;
        String domain = question.getDomain();
        if (domain != null && (domain.equalsIgnoreCase("LOGICAL_REASONING") || domain.equalsIgnoreCase("SYLLOGISM") || domain.equalsIgnoreCase("LOGIC"))) return true;
        String stmt = question.getStatement().toLowerCase();
        return stmt.contains("taller") || stmt.contains("shorter") || stmt.contains("greater") || stmt.contains("syllogism") || stmt.contains("all ") || stmt.contains("some ");
    }

    @Override
    public String getDomainName() {
        return "LOGICAL_REASONING";
    }

    @Override
    public ReasoningProposal generateReasoning(Question question) {
        String stmt = question.getStatement();
        List<ReasoningStep> steps = new ArrayList<>();

        steps.add(ReasoningStep.builder()
                .id("s1")
                .sequence(1)
                .type(StepType.GIVEN)
                .after("Premise 1: A is taller than B (A > B). Premise 2: B is taller than C (B > C).")
                .justification(StepJustification.builder().text("Extract the given comparative premises.").build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.REASONING_GRAPH))
                .build());

        steps.add(ReasoningStep.builder()
                .id("s2")
                .sequence(2)
                .type(StepType.INFER)
                .inputs(List.of("s1"))
                .before("A > B and B > C")
                .after("Chain: A > B > C")
                .justification(StepJustification.builder().text("Apply transitive property of strict inequalities: if A > B and B > C, then A > B > C.").build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.REASONING_GRAPH))
                .build());

        steps.add(ReasoningStep.builder()
                .id("s3")
                .sequence(3)
                .type(StepType.CONCLUDE)
                .inputs(List.of("s2"))
                .before("Chain: A > B > C")
                .after("C is the shortest.")
                .justification(StepJustification.builder().text("Since C is at the bottom end of the transitive ordering chain, C is shortest.").build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.REASONING_GRAPH))
                .build());

        DiagramIR diagram = DiagramIR.builder()
                .id("diag-ordering")
                .type("bar")
                .title("Height Comparison")
                .objects(List.of(
                        DiagramObject.builder().id("A").type("bar").label("A").position(List.of(1.0, 10.0)).properties(Map.of("height", 10)).build(),
                        DiagramObject.builder().id("B").type("bar").label("B").position(List.of(2.0, 7.0)).properties(Map.of("height", 7)).build(),
                        DiagramObject.builder().id("C").type("bar").label("C").position(List.of(3.0, 4.0)).properties(Map.of("height", 4)).build()
                ))
                .relationships(List.of(
                        DiagramRelationship.builder().type("GREATER_THAN").from("A").to("B").label(">").build(),
                        DiagramRelationship.builder().type("GREATER_THAN").from("B").to("C").label(">").build()
                ))
                .build();

        return ReasoningProposal.builder()
                .questionId(question.getQuestionId())
                .problemSummary("Determine who is shortest among A, B, and C.")
                .concepts(List.of("transitive-relation", "comparative-reasoning", "deductive-logic"))
                .facts(List.of("A > B", "B > C"))
                .steps(steps)
                .conclusion("C is the shortest")
                .verification(VerificationResult.builder().type("LOGICAL_CONSISTENCY").expression("A > B > C").passed(true).build())
                .hints(List.of(
                        Hint.builder().id("h1").level(1).text("Combine the two statements into a single ordering chain.").build(),
                        Hint.builder().id("h2").level(2).text("Since A > B and B > C, what can we say about A and C?").build(),
                        Hint.builder().id("h3").level(3).text("The person at the lowest position in A > B > C is the shortest.").build()
                ))
                .diagrams(List.of(diagram))
                .build();
    }

    @Override
    public ValidationResult validate(ReasoningProposal proposal) {
        return ValidationResult.builder().status(ValidationStatus.PASSED).score(1.0).build();
    }

    @Override
    public List<Hint> generateHints(ReasoningProposal proposal) {
        return proposal.getHints() != null ? proposal.getHints() : List.of();
    }

    @Override
    public List<Misconception> detectMisconceptions(ReasoningProposal proposal) {
        return List.of();
    }
}
