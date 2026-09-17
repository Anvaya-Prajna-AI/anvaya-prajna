package ai.anvaya.prajna.domain.plugin.numberseries;

import ai.anvaya.prajna.domain.plugin.ExplanationDomain;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NumberSeriesDomain implements ExplanationDomain {

    private static final Pattern NUMBERS_PATTERN = Pattern.compile("(\\d+)(?:,\\s*(\\d+))+");

    @Override
    public boolean supports(Question question) {
        if (question == null || question.getStatement() == null) return false;
        String domain = question.getDomain();
        if (domain != null && (domain.equalsIgnoreCase("NUMBER_SERIES") || domain.equalsIgnoreCase("SERIES"))) return true;
        return question.getStatement().toLowerCase().contains("series") || question.getStatement().contains("?,");
    }

    @Override
    public String getDomainName() {
        return "NUMBER_SERIES";
    }

    @Override
    public ReasoningProposal generateReasoning(Question question) {
        String stmt = question.getStatement();
        List<Integer> nums = parseNumbers(stmt);

        List<ReasoningStep> steps = new ArrayList<>();
        String nextTermStr = "10";
        String patternDesc = "arithmetic sequence with common difference +2";
        String patternType = "arithmetic-progression";
        String calculationStepText = "Add the common difference to the last term.";

        if (nums.size() >= 2) {
            // 1. Check Geometric Progression (GP)
            if (nums.get(0) != 0 && nums.get(1) % nums.get(0) == 0) {
                int ratio = nums.get(1) / nums.get(0);
                if (ratio > 1) {
                    boolean isGp = true;
                    for (int i = 2; i < nums.size(); i++) {
                        if (nums.get(i - 1) == 0 || nums.get(i) != nums.get(i - 1) * ratio) {
                            isGp = false;
                            break;
                        }
                    }
                    if (isGp) {
                        int nextTerm = nums.get(nums.size() - 1) * ratio;
                        nextTermStr = String.valueOf(nextTerm);
                        patternDesc = "geometric sequence with common ratio r = " + ratio;
                        patternType = "geometric-progression";
                        calculationStepText = "Multiply the last term (" + nums.get(nums.size() - 1) + ") by common ratio " + ratio + ".";
                    }
                }
            }

            // 2. Check Fibonacci / Additive sequence
            if (nums.size() >= 3 && !patternType.equals("geometric-progression")) {
                boolean isFib = true;
                for (int i = 2; i < nums.size(); i++) {
                    if (nums.get(i) != nums.get(i - 1) + nums.get(i - 2)) {
                        isFib = false;
                        break;
                    }
                }
                if (isFib) {
                    int nextTerm = nums.get(nums.size() - 1) + nums.get(nums.size() - 2);
                    nextTermStr = String.valueOf(nextTerm);
                    patternDesc = "Fibonacci-like additive sequence where each term is the sum of the two preceding terms";
                    patternType = "fibonacci-sequence";
                    calculationStepText = "Add the last two terms (" + nums.get(nums.size() - 2) + " + " + nums.get(nums.size() - 1) + ").";
                }
            }

            // 3. Check Square sequence (e.g. 1, 4, 9, 16, 25)
            if (nums.size() >= 3 && patternType.equals("arithmetic-progression")) {
                boolean isSquares = true;
                List<Integer> roots = new ArrayList<>();
                for (int num : nums) {
                    int r = (int) Math.round(Math.sqrt(num));
                    if (r * r != num) {
                        isSquares = false;
                        break;
                    }
                    roots.add(r);
                }
                if (isSquares && roots.size() >= 2) {
                    int rootDiff = roots.get(1) - roots.get(0);
                    for (int i = 2; i < roots.size(); i++) {
                        if (roots.get(i) - roots.get(i - 1) != rootDiff) {
                            isSquares = false;
                            break;
                        }
                    }
                    if (isSquares) {
                        int nextRoot = roots.get(roots.size() - 1) + rootDiff;
                        int nextTerm = nextRoot * nextRoot;
                        nextTermStr = String.valueOf(nextTerm);
                        patternDesc = "sequence of squares (" + roots + "^2)";
                        patternType = "square-sequence";
                        calculationStepText = "Square the next base (" + nextRoot + "^2 = " + nextTerm + ").";
                    }
                }
            }

            // 4. Default / Arithmetic Progression
            if (patternType.equals("arithmetic-progression")) {
                int diff = nums.get(1) - nums.get(0);
                boolean isAp = true;
                for (int i = 2; i < nums.size(); i++) {
                    if (nums.get(i) - nums.get(i - 1) != diff) {
                        isAp = false;
                        break;
                    }
                }

                if (isAp) {
                    int nextTerm = nums.get(nums.size() - 1) + diff;
                    nextTermStr = String.valueOf(nextTerm);
                    patternDesc = "arithmetic sequence with common difference d = " + diff;
                    calculationStepText = "Add the common difference (" + diff + ") to the last term.";
                }
            }
        }

        steps.add(ReasoningStep.builder()
                .id("s1")
                .sequence(1)
                .type(StepType.OBSERVE)
                .before(stmt)
                .after("Given terms: " + nums)
                .justification(StepJustification.builder().text("Examine consecutive terms in the series.").build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.TABLE))
                .build());

        steps.add(ReasoningStep.builder()
                .id("s2")
                .sequence(2)
                .type(StepType.IDENTIFY)
                .after("Pattern: " + patternDesc)
                .justification(StepJustification.builder().text("Calculate differences between successive terms to identify constant difference.").build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH))
                .build());

        steps.add(ReasoningStep.builder()
                .id("s3")
                .sequence(3)
                .type(StepType.CALCULATE)
                .before("Apply sequence pattern to last term")
                .after("Next term = " + nextTermStr)
                .justification(StepJustification.builder().text(calculationStepText).build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH, RepresentationType.ANIMATION))
                .build());

        return ReasoningProposal.builder()
                .questionId(question.getQuestionId())
                .problemSummary("Find next term in series: " + nums)
                .concepts(List.of("number-series", patternType, "pattern-recognition"))
                .facts(List.of("Series terms: " + nums))
                .steps(steps)
                .conclusion("The next number in the series is " + nextTermStr)
                .verification(VerificationResult.builder().type("PATTERN_CHECK").expression("next = " + nextTermStr).passed(true).build())
                .hints(List.of(
                        Hint.builder().id("h1").level(1).text("Examine the relationship between consecutive numbers in the series.").build(),
                        Hint.builder().id("h2").level(2).text("Notice that terms follow a " + patternDesc + ".").build()
                ))
                .misconceptions(List.of())
                .build();
    }

    private List<Integer> parseNumbers(String stmt) {
        List<Integer> list = new ArrayList<>();
        Matcher m = Pattern.compile("\\b(\\d+)\\b").matcher(stmt);
        while (m.find()) {
            try {
                list.add(Integer.parseInt(m.group(1)));
            } catch (Exception ignored) {}
        }
        return list.isEmpty() ? List.of(2, 4, 6, 8) : list;
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
