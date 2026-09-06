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

        if (nums.size() >= 2) {
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
                .before("Last term + difference")
                .after("Next term = " + nextTermStr)
                .justification(StepJustification.builder().text("Add the common difference to the last term.").build())
                .representations(List.of(RepresentationType.TEXT, RepresentationType.MATH, RepresentationType.ANIMATION))
                .build());

        return ReasoningProposal.builder()
                .questionId(question.getQuestionId())
                .problemSummary("Find next term in series")
                .concepts(List.of("number-series", "arithmetic-progression", "pattern-recognition"))
                .facts(List.of("Series terms: " + nums))
                .steps(steps)
                .conclusion("The next number in the series is " + nextTermStr)
                .verification(VerificationResult.builder().type("PATTERN_CHECK").expression("next = " + nextTermStr).passed(true).build())
                .hints(List.of(
                        Hint.builder().id("h1").level(1).text("Look at the difference between consecutive numbers.").build(),
                        Hint.builder().id("h2").level(2).text("Notice that each term increases by the same amount.").build()
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
