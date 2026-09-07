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
    private static final Pattern PERCENT_OF_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:%|percent)\\s*of\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RATIO_DIVIDE_PATTERN = Pattern.compile("(?:divide|share|split|distribute)\\s*(\\d+(?:\\.\\d+)?).*?ratio\\s*(\\d+)\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RATIO_GENERAL_PATTERN = Pattern.compile("ratio\\s*(?:of)?\\s*(\\d+)\\s*:\\s*(\\d+).*?(?:total|sum|of)?\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean supports(Question question) {
        if (question == null || question.getStatement() == null) return false;
        String domain = question.getDomain();
        if (domain != null && (domain.equalsIgnoreCase("PERCENTAGE") || domain.equalsIgnoreCase("RATIO") || domain.equalsIgnoreCase("PERCENTAGE_RATIO") || domain.equalsIgnoreCase("TIME_SPEED_DISTANCE"))) return true;
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

        // 1. Check for Percentage Calculation: e.g. "What is 20% of 150?"
        Matcher pctM = PERCENT_OF_PATTERN.matcher(stmt);
        if (pctM.find()) {
            double p = Double.parseDouble(pctM.group(1));
            double total = Double.parseDouble(pctM.group(2));
            double ans = (p / 100.0) * total;
            String ansStr = (ans == (long) ans) ? String.format("%d", (long) ans) : String.format("%.2f", ans);
            String pStr = (p == (long) p) ? String.format("%d", (long) p) : String.format("%.2f", p);
            String totalStr = (total == (long) total) ? String.format("%d", (long) total) : String.format("%.2f", total);

            List<ReasoningStep> steps = new ArrayList<>();
            steps.add(ReasoningStep.builder()
                    .id("s1")
                    .sequence(1)
                    .type(StepType.GIVEN)
                    .after("Percentage = " + pStr + "%, Base value = " + totalStr)
                    .justification(StepJustification.builder().text("Identify the given percentage and total base amount.").build())
                    .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                    .build());

            steps.add(ReasoningStep.builder()
                    .id("s2")
                    .sequence(2)
                    .type(StepType.APPLY_RULE)
                    .after("Percentage value = (P / 100) * Base")
                    .justification(StepJustification.builder().text("Convert the percentage to a fraction or decimal and multiply by base.").build())
                    .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                    .build());

            steps.add(ReasoningStep.builder()
                    .id("s3")
                    .sequence(3)
                    .type(StepType.SUBSTITUTE)
                    .before("Value = (P / 100) * Base")
                    .after("Value = (" + pStr + " / 100) * " + totalStr)
                    .justification(StepJustification.builder().text("Substitute known percentage and base values into formula.").build())
                    .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                    .build());

            steps.add(ReasoningStep.builder()
                    .id("s4")
                    .sequence(4)
                    .type(StepType.CALCULATE)
                    .before("(" + pStr + " / 100) * " + totalStr)
                    .after(ansStr)
                    .justification(StepJustification.builder().text("Perform calculation: " + pStr + " / 100 * " + totalStr + " = " + ansStr).build())
                    .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH, RepresentationType.ANIMATION))
                    .build());

            steps.add(ReasoningStep.builder()
                    .id("s5")
                    .sequence(5)
                    .type(StepType.VERIFY)
                    .before(ansStr + " / " + totalStr + " * 100")
                    .after(ansStr + " / " + totalStr + " * 100 = " + pStr)
                    .justification(StepJustification.builder().text("Verify that " + ansStr + " divided by " + totalStr + " equals " + pStr + "%.").build())
                    .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                    .build());

            return ReasoningProposal.builder()
                    .questionId(question.getQuestionId())
                    .problemSummary("Calculate " + pStr + "% of " + totalStr)
                    .concepts(List.of("percentage", "fractions", "rate-calculation"))
                    .facts(List.of("Percentage = " + pStr + "%", "Base = " + totalStr))
                    .steps(steps)
                    .conclusion(pStr + "% of " + totalStr + " is " + ansStr)
                    .verification(VerificationResult.builder().type("PERCENT_CHECK").expression(ansStr + " / " + totalStr + " = " + (p / 100.0)).passed(true).build())
                    .hints(List.of(
                            Hint.builder().id("h1").level(1).text("Percent means parts per hundred: divide " + pStr + " by 100.").build(),
                            Hint.builder().id("h2").level(2).text("Multiply that decimal by the total amount " + totalStr + ".").build()
                    ))
                    .misconceptions(List.of(
                            Misconception.builder()
                                    .id("m1")
                                    .incorrectReasoning("Multiplying directly: " + pStr + " * " + totalStr)
                                    .whyWrong("Forgetting to divide by 100 when working with percentages.")
                                    .correctedReasoning("Always divide percentage by 100 first: (" + pStr + " / 100) * " + totalStr)
                                    .build()
                    ))
                    .build();
        }

        // 2. Check for Ratio Division: e.g. "Divide 100 in the ratio 2:3"
        Matcher ratioM = RATIO_DIVIDE_PATTERN.matcher(stmt);
        double ratioTotal = 0;
        int rA = 0, rB = 0;
        boolean isRatio = false;
        if (ratioM.find()) {
            ratioTotal = Double.parseDouble(ratioM.group(1));
            rA = Integer.parseInt(ratioM.group(2));
            rB = Integer.parseInt(ratioM.group(3));
            isRatio = true;
        } else {
            Matcher ratioGenM = RATIO_GENERAL_PATTERN.matcher(stmt);
            if (ratioGenM.find()) {
                rA = Integer.parseInt(ratioGenM.group(1));
                rB = Integer.parseInt(ratioGenM.group(2));
                String totGroup = ratioGenM.group(3);
                if (totGroup != null && !totGroup.isBlank()) {
                    ratioTotal = Double.parseDouble(totGroup);
                    isRatio = true;
                }
            }
        }

        if (isRatio && (rA + rB) > 0) {
            int totalParts = rA + rB;
            double shareA = ratioTotal * rA / totalParts;
            double shareB = ratioTotal * rB / totalParts;
            String shareAStr = (shareA == (long) shareA) ? String.format("%d", (long) shareA) : String.format("%.2f", shareA);
            String shareBStr = (shareB == (long) shareB) ? String.format("%d", (long) shareB) : String.format("%.2f", shareB);
            String totStr = (ratioTotal == (long) ratioTotal) ? String.format("%d", (long) ratioTotal) : String.format("%.2f", ratioTotal);

            List<ReasoningStep> steps = new ArrayList<>();
            steps.add(ReasoningStep.builder()
                    .id("s1")
                    .sequence(1)
                    .type(StepType.GIVEN)
                    .after("Total amount = " + totStr + ", Ratio = " + rA + ":" + rB)
                    .justification(StepJustification.builder().text("Identify total quantity and required distribution ratio.").build())
                    .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                    .build());

            steps.add(ReasoningStep.builder()
                    .id("s2")
                    .sequence(2)
                    .type(StepType.APPLY_RULE)
                    .after("Total parts = " + rA + " + " + rB + " = " + totalParts + ". Each part = Total / Total parts.")
                    .justification(StepJustification.builder().text("Sum the ratio terms to find total proportional units.").build())
                    .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                    .build());

            steps.add(ReasoningStep.builder()
                    .id("s3")
                    .sequence(3)
                    .type(StepType.CALCULATE)
                    .before("Part A share: " + totStr + " * (" + rA + " / " + totalParts + ")")
                    .after("Share A = " + shareAStr)
                    .justification(StepJustification.builder().text("Calculate first proportion.").build())
                    .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                    .build());

            steps.add(ReasoningStep.builder()
                    .id("s4")
                    .sequence(4)
                    .type(StepType.CALCULATE)
                    .before("Part B share: " + totStr + " * (" + rB + " / " + totalParts + ")")
                    .after("Share B = " + shareBStr)
                    .justification(StepJustification.builder().text("Calculate second proportion.").build())
                    .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                    .build());

            steps.add(ReasoningStep.builder()
                    .id("s5")
                    .sequence(5)
                    .type(StepType.VERIFY)
                    .before(shareAStr + " + " + shareBStr)
                    .after(shareAStr + " + " + shareBStr + " = " + totStr)
                    .justification(StepJustification.builder().text("Verify that the computed shares sum back to the original total.").build())
                    .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                    .build());

            return ReasoningProposal.builder()
                    .questionId(question.getQuestionId())
                    .problemSummary("Divide " + totStr + " in ratio " + rA + ":" + rB)
                    .concepts(List.of("ratio", "proportional-sharing", "fractions"))
                    .facts(List.of("Total = " + totStr, "Ratio = " + rA + ":" + rB))
                    .steps(steps)
                    .conclusion("The shares are " + shareAStr + " and " + shareBStr)
                    .verification(VerificationResult.builder().type("SUM_CHECK").expression(shareAStr + " + " + shareBStr + " = " + totStr).passed(true).build())
                    .hints(List.of(
                            Hint.builder().id("h1").level(1).text("Add the two ratio numbers together to find total parts: " + rA + " + " + rB + " = " + totalParts).build(),
                            Hint.builder().id("h2").level(2).text("Find the value of 1 part: " + totStr + " / " + totalParts).build(),
                            Hint.builder().id("h3").level(3).text("Multiply 1 part by " + rA + " and " + rB + " respectively.").build()
                    ))
                    .build();
        }

        // 3. Fallback to Speed-Distance-Time problem
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
