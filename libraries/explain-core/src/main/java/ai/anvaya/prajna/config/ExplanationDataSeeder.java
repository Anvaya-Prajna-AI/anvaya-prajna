package ai.anvaya.prajna.config;

import ai.anvaya.prajna.application.ExplanationService;
import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.StudentLevel;
import ai.anvaya.prajna.repository.ExplanationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExplanationDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ExplanationDataSeeder.class);

    private final ExplanationService explanationService;
    private final ExplanationRepository explanationRepository;

    public ExplanationDataSeeder(ExplanationService explanationService, ExplanationRepository explanationRepository) {
        this.explanationService = explanationService;
        this.explanationRepository = explanationRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Checking and seeding educational explanations...");

        List<Question> seedQuestions = List.of(
                Question.builder()
                        .questionId("q-alg-101")
                        .statement("Solve for x: 3x + 5 = 20")
                        .domain("ALGEBRA")
                        .difficulty("MEDIUM")
                        .authoritativeAnswer("5")
                        .build(),
                Question.builder()
                        .questionId("q-syl-202")
                        .statement("John is taller than Alice. Alice is taller than Bob. Who is the tallest?")
                        .domain("LOGICAL_REASONING")
                        .difficulty("EASY")
                        .authoritativeAnswer("John")
                        .build(),
                Question.builder()
                        .questionId("q-speed-202")
                        .statement("A car travels 120 km in 2 hours. What is its speed?")
                        .domain("PERCENTAGE_RATIO")
                        .difficulty("EASY")
                        .authoritativeAnswer("60")
                        .build(),
                Question.builder()
                        .questionId("q-pct-303")
                        .statement("What is 20% of 150?")
                        .domain("PERCENTAGE")
                        .difficulty("EASY")
                        .authoritativeAnswer("30")
                        .build(),
                Question.builder()
                        .questionId("q-series-404")
                        .statement("3, 6, 12, 24, ?")
                        .domain("NUMBER_SERIES")
                        .difficulty("MEDIUM")
                        .authoritativeAnswer("48")
                        .build(),
                Question.builder()
                        .questionId("q-ratio-505")
                        .statement("Divide 100 in the ratio 2:3")
                        .domain("RATIO")
                        .difficulty("MEDIUM")
                        .authoritativeAnswer("40 and 60")
                        .build()
        );

        ExplanationPolicy policy = ExplanationPolicy.builder()
                .level(StudentLevel.INTERMEDIATE)
                .showWhy(true)
                .showVerification(true)
                .showMisconceptions(true)
                .animation("FULL")
                .build();

        for (Question q : seedQuestions) {
            if (explanationRepository.findFirstByQuestionIdOrderByVersionDesc(q.getQuestionId()).isPresent()) {
                log.info("Explanation for {} already exists. Skipping.", q.getQuestionId());
                continue;
            }
            try {
                explanationService.generateExplanation(q, policy);
                log.info("Successfully seeded explanation for: {} ({})", q.getQuestionId(), q.getStatement());
            } catch (Exception e) {
                log.error("Failed to seed explanation for {}: {}", q.getQuestionId(), e.getMessage());
            }
        }

        log.info("Finished seeding explanations. Total in database: {}", explanationRepository.count());
    }
}