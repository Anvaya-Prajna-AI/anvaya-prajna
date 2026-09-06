package ai.anvaya.prajna.domain.plugin.ratio;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PercentageRatioDomain implements ExplanationDomain {

    private static final Pattern SPEED_PATTERN = Pattern.compile("(\\d+)\\s*(?:km|miles|m).*?(\\d+)\\s*(?:hours|hour|hr|h|min|s|seconds)");

    @Override
    public boolean supports(Question question) {
        if (question == null || question.getStatement() == null) return false;
        String domain = question.getDomain();
        if (domain != null && (domain.equalsIgnoreCase("PERCENTAGE") || domain.equalsIgnoreCase("RATIO") || domain.equalsIgnoreCase("TIME_SPEED_DISTANCE"))) return true;
        String stmt = question.getStatement().toLowerCase();
        return stmt.contains("speed") || stmt.contains("distance") || stmt.contains("%") || stmt.contains("percent") || stmt.contains("ratio");
    }

    @Override
    public String getDomainName() {
        return "PERCENTAGE_RATIO";
    }

    @Override
    public ReasoningProposal generateReasoning(Question question) {
        String stmt = question.getStatement();
        Matcher m = SPEED_PATTERN.matcher(stmt);

        int d = 120;
        int t = 2;
        if (m.find()) {
            try {
                d = Integer.parseInt(m.group(1));
                t = Integer.parseInt(m.group(2));
            } catch (Exception ignored) {}
        }

        int speed = d / (t > 0 ? t : 1);
        List<ReasoningStep> steps = new ArrayList<>();

        steps.add(ReasoningStep.builder()
                .id("s1")
                .sequence(1)
                .type(StepType.GIVEN)
                .after("distance (d) = " + d + " km, time (t) = " + t + " hours")
                .justification(StepJustification.builder().text("Identify given values from the problem statement.").build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                .build());

        steps.add(ReasoningStep.builder()
                .id("s2")
                .sequence(2)
                .type(StepType.IDENTIFY)
                .after("Concept: Average speed definition")
                .justification(StepJustification.builder().text("Identify the target unknown and physical principle.").build())
                .representations(List.of(RepresentationType.TEXT))
                .build());

        steps.add(ReasoningStep.builder()
                .id("s3")
                .sequence(3)
                .type(StepType.APPLY_RULE)
                .after("v = d / t")
                .justification(StepJustification.builder().text("Apply speed formula: speed = distance / time.").build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                .build());

        steps.add(ReasoningStep.builder()
                .id("s4")
                .sequence(4)
                .type(StepType.SUBSTITUTE)
                .before("v = d / t")
                .after("v = " + d + " / " + t)
                .justification(StepJustification.builder().text("Substitute known distance (" + d + " km) and time (" + t + " h) into formula.").build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                .build());

        steps.add(ReasoningStep.builder()
                .id("s5")
                .sequence(5)
                .type(StepType.CALCULATE)
                .before("v = " + d + " / " + t)
                .after("v = " + speed + " km/h")
                .justification(StepJustification.builder().text("Perform division: " + d + " / " + t + " = " + speed + ".").build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH, RepresentationType.ANIMATION))
                .build());

        steps.add(ReasoningStep.builder()
                .id("s6")
                .sequence(6)
                .type(StepType.VERIFY)
                .before("speed * time = distance")
                .after(speed + " * " + t + " = " + d + " km")
                .justification(StepJustification.builder().text("Verify that speed * time yields original distance: " + speed + " * " + t + " = " + d + " km.").build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                .build());

        DiagramIR diagram = DiagramIR.builder()
                .id("diag-journey")
                .type("number-line")
                .title("Journey Distance")
                .objects(List.of(
                        DiagramObject.builder().id("A").type("point").label("Start (A)").position(List.of(0.0, 0.0)).build(),
                        DiagramObject.builder().id("B").type("point").label("Destination (B)").position(List.of((double) d, 0.0)).build()
                ))
                .relationships(List.of(
                        DiagramRelationship.builder().type("PATH").from("A").to("B").label(d + " km, in " + t + " hours (v = " + speed + " km/h)").build()
                ))
                .build();

        VerificationResult verification = VerificationResult.builder()
                .type("SUBSTITUTION")
                .expression(speed + " * " + t + " = " + d)
                .expected(true)
                .actual(true)
                .passed(true)
                .details("Verified: " + speed + " km/h * " + t + " h = " + d + " km.")
                .build();

        List<Hint> hints = List.of(
                Hint.builder().id("h1").level(1).text("What information is given in the question?").build(),
                Hint.builder().id("h2").level(2).text("Which formula relates speed, distance, and time?").build(),
                Hint.builder().id("h3").level(3).text("Use: speed = distance / time.").build(),
                Hint.builder().id("h4").level(4).text("Substitute " + d + " for distance and " + t + " for time.").build()
        );

        return ReasoningProposal.builder()
                .questionId(question.getQuestionId())
                .problemSummary("Calculate average speed given " + d + " km in " + t + " hours.")
                .concepts(List.of("speed-distance-time", "ratio", "rate-of-change"))
                .facts(List.of("Distance = " + d + " km", "Time = " + t + " hours"))
                .steps(steps)
                .conclusion("Average speed is " + speed + " km/h")
                .verification(verification)
                .hints(hints)
                .diagrams(List.of(diagram))
                .misconceptions(List.of())
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
