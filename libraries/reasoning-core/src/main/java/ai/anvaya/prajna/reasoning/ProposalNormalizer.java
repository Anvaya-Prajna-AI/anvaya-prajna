package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.MathExpression;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.StepJustification;
import ai.anvaya.prajna.ir.StepType;
import ai.anvaya.prajna.math.MathAstBuilder;

import java.util.ArrayList;
import java.util.List;

public class ProposalNormalizer {

    private final MathAstBuilder mathAstBuilder = new MathAstBuilder();

    public ReasoningProposal normalize(ReasoningProposal proposal) {
        if (proposal == null) {
            return ReasoningProposal.builder().steps(List.of()).build();
        }

        List<ReasoningStep> normalizedSteps = new ArrayList<>();
        if (proposal.getSteps() != null) {
            int seq = 1;
            for (ReasoningStep step : proposal.getSteps()) {
                ReasoningStep normalized = normalizeStep(step, seq++);
                normalizedSteps.add(normalized);
            }
        }

        return ReasoningProposal.builder()
                .questionId(proposal.getQuestionId())
                .problemSummary(proposal.getProblemSummary() != null ? proposal.getProblemSummary().trim() : null)
                .concepts(proposal.getConcepts() != null ? proposal.getConcepts() : List.of())
                .facts(proposal.getFacts() != null ? proposal.getFacts() : List.of())
                .assumptions(proposal.getAssumptions() != null ? proposal.getAssumptions() : List.of())
                .steps(normalizedSteps)
                .conclusion(proposal.getConclusion() != null ? proposal.getConclusion().trim() : null)
                .verification(proposal.getVerification())
                .hints(proposal.getHints() != null ? proposal.getHints() : List.of())
                .misconceptions(proposal.getMisconceptions() != null ? proposal.getMisconceptions() : List.of())
                .diagrams(proposal.getDiagrams() != null ? proposal.getDiagrams() : List.of())
                .build();
    }

    private ReasoningStep normalizeStep(ReasoningStep step, int sequence) {
        String id = step.getId() != null && !step.getId().isBlank() ? step.getId() : "s" + sequence;
        StepType type = step.getType() != null ? step.getType() : StepType.INFER;

        StepJustification justification = step.getJustification();
        if (justification == null && step.getAfter() != null) {
            justification = StepJustification.builder().text(step.getAfter()).depth(1).build();
        }

        MathExpression math = step.getMath();
        if (math == null && (step.getBefore() != null || step.getAfter() != null)) {
            String exprText = step.getAfter() != null ? step.getAfter() : step.getBefore();
            math = mathAstBuilder.build(exprText);
        }

        return ReasoningStep.builder()
                .id(id)
                .sequence(sequence)
                .type(type)
                .inputs(step.getInputs() != null ? step.getInputs() : List.of())
                .operation(step.getOperation())
                .before(step.getBefore() != null ? step.getBefore().trim() : null)
                .after(step.getAfter() != null ? step.getAfter().trim() : null)
                .outputs(step.getOutputs() != null ? step.getOutputs() : List.of())
                .justification(justification)
                .representations(step.getRepresentations() != null ? step.getRepresentations() : new ArrayList<>())
                .math(math)
                .verification(step.getVerification())
                .misconception(step.getMisconception())
                .build();
    }
}
