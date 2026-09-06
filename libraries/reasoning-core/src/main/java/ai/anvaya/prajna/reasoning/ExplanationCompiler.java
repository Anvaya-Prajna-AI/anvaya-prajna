package ai.anvaya.prajna.reasoning;

import ai.anvaya.prajna.ir.AnimationIR;
import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.ExplanationMetadata;
import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.ExplanationStatus;
import ai.anvaya.prajna.ir.Problem;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.ir.ReasoningGraphIR;
import ai.anvaya.prajna.ir.ReasoningStep;
import ai.anvaya.prajna.ir.VerificationResult;

import java.util.List;
import java.util.UUID;

public class ExplanationCompiler {

    private final ProposalNormalizer normalizer;
    private final PedagogicalPlanner pedagogicalPlanner;
    private final RepresentationPlanner representationPlanner;
    private final ReasoningGraphBuilder graphBuilder;
    private final AnimationCompiler animationCompiler;

    public ExplanationCompiler() {
        this.normalizer = new ProposalNormalizer();
        this.pedagogicalPlanner = new PedagogicalPlanner();
        this.representationPlanner = new RepresentationPlanner();
        this.graphBuilder = new ReasoningGraphBuilder();
        this.animationCompiler = new AnimationCompiler();
    }

    public ExplanationCompiler(ProposalNormalizer normalizer,
                               PedagogicalPlanner pedagogicalPlanner,
                               RepresentationPlanner representationPlanner,
                               ReasoningGraphBuilder graphBuilder,
                               AnimationCompiler animationCompiler) {
        this.normalizer = normalizer;
        this.pedagogicalPlanner = pedagogicalPlanner;
        this.representationPlanner = representationPlanner;
        this.graphBuilder = graphBuilder;
        this.animationCompiler = animationCompiler;
    }

    public ExplanationIR compile(Question question, ReasoningProposal proposal, ExplanationPolicy policy) {
        long startTime = System.currentTimeMillis();

        // 1. Normalize
        ReasoningProposal normalizedProposal = normalizer.normalize(proposal);

        // 2. Pedagogical planning
        List<ReasoningStep> plannedSteps = pedagogicalPlanner.planSteps(normalizedProposal.getSteps(), policy);

        // 3. Representation planning
        String domain = question != null && question.getDomain() != null ? question.getDomain() : "GENERAL";
        List<ReasoningStep> representedSteps = representationPlanner.assignRepresentations(plannedSteps, domain, policy);

        // 4. Reasoning graph
        ReasoningGraphIR graph = graphBuilder.buildGraph(
                normalizedProposal.getFacts(),
                representedSteps,
                normalizedProposal.getConclusion()
        );

        // 5. Animation timeline
        AnimationIR animation = null;
        if (policy == null || !"NONE".equalsIgnoreCase(policy.getAnimation())) {
            animation = animationCompiler.compile(representedSteps);
        }

        // 6. Problem definition
        Problem problem = Problem.builder()
                .statement(question != null ? question.getStatement() : normalizedProposal.getProblemSummary())
                .domain(domain)
                .difficulty(question != null ? question.getDifficulty() : "MEDIUM")
                .authoritativeAnswer(question != null ? question.getAuthoritativeAnswer() : null)
                .options(question != null ? question.getOptions() : null)
                .build();

        String explanationId = "exp-" + UUID.randomUUID().toString().substring(0, 8);
        String questionId = question != null ? question.getQuestionId() : normalizedProposal.getQuestionId();

        long latency = System.currentTimeMillis() - startTime;

        ExplanationMetadata metadata = ExplanationMetadata.builder()
                .language("en")
                .studentLevel(policy != null ? policy.getLevel() : null)
                .compilerVersion("1.0.0")
                .modelVersion("1.0")
                .generationLatencyMs(latency)
                .qualityScore(0.95)
                .build();

        return ExplanationIR.builder()
                .schemaVersion("1.0")
                .explanationId(explanationId)
                .questionId(questionId)
                .status(ExplanationStatus.DRAFT)
                .problem(problem)
                .concepts(normalizedProposal.getConcepts())
                .facts(normalizedProposal.getFacts())
                .assumptions(normalizedProposal.getAssumptions())
                .reasoningGraph(graph)
                .steps(representedSteps)
                .verification(normalizedProposal.getVerification())
                .hints(normalizedProposal.getHints())
                .misconceptions(normalizedProposal.getMisconceptions())
                .diagrams(normalizedProposal.getDiagrams())
                .animation(animation)
                .metadata(metadata)
                .build();
    }
}
