package ai.anvaya.prajna.application;

import ai.anvaya.prajna.ir.ExplanationIR;
import ai.anvaya.prajna.ir.ExplanationPolicy;
import ai.anvaya.prajna.ir.Question;
import ai.anvaya.prajna.reasoning.ExplanationCompiler;
import ai.anvaya.prajna.reasoning.ReasoningProposal;
import org.springframework.stereotype.Service;

@Service
public class CompilationService {

    private final ExplanationCompiler explanationCompiler;

    public CompilationService() {
        this.explanationCompiler = new ExplanationCompiler();
    }

    public ExplanationIR compileExplanation(Question question, ReasoningProposal proposal, ExplanationPolicy policy) {
        return explanationCompiler.compile(question, proposal, policy);
    }
}
