package ai.anvaya.prajna.ai;

import ai.anvaya.prajna.ir.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OWASP Top 10 for LLMs and AI Agents Security Guard.
 * Implements defenses for:
 * - LLM01: Prompt Injection (Direct & Indirect jailbreak pattern detection & isolation)
 * - LLM02: Sensitive Information Disclosure (PII & API token scrubbing)
 * - LLM03: Supply Chain Vulnerabilities (Verified Bedrock Nova & Gemini model whitelisting)
 * - LLM05: Improper Output Handling (Markdown stripping, XSS script neutralization)
 * - LLM07: System Prompt Leakage (Reflection & instruction leak detection)
 * - LLM08 / LLM10: Context Exhaustion & Denial of Service (Input length & token bounding)
 */
@Component
public class OwaspAgentSecurityGuard {

    private static final Logger log = LoggerFactory.getLogger(OwaspAgentSecurityGuard.class);

    // Maximum question statement length to mitigate Denial of Service / Context Exhaustion (LLM08, LLM10)
    public static final int MAX_STATEMENT_LENGTH = 4000;

    // Approved Model Whitelist for Bedrock Nova & Google Gemini (LLM03)
    private static final Set<String> APPROVED_MODELS = new HashSet<>(List.of(
            // Amazon Bedrock Nova Family
            "amazon.nova-pro-v1:0",
            "amazon.nova-lite-v1:0",
            "amazon.nova-micro-v1:0",
            "amazon-nova-pro",
            "amazon-nova-lite",
            "amazon-nova-micro",
            // Google Gemini Family
            "gemini-1.5-pro",
            "gemini-1.5-flash",
            "gemini-2.0-pro",
            "gemini-2.0-flash",
            "gemini-2.0-flash-exp",
            // Default gateway router targets
            "gpt-4o-mini",
            "gpt-4o"
    ));

    // Prompt Injection Patterns (LLM01)
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)\\bignore\\s+(all\\s+)?(previous|prior)\\s+(instructions|prompts|directives)\\b"),
            Pattern.compile("(?i)\\b(disregard\\s+(all\\s+)?(previous|prior|above)\\s+(instructions|rules))\\b"),
            Pattern.compile("(?i)\\byou\\s+are\\s+now\\s+(in\\s+)?(developer\\s+mode|dan|jailbreak|unrestricted|god\\s+mode)\\b"),
            Pattern.compile("(?i)\\b(system\\s+override|override\\s+system|admin\\s+override)\\b"),
            Pattern.compile("(?i)\\b(repeat\\s+(all\\s+)?(words\\s+above|system\\s+prompt|instructions\\s+above))\\b"),
            Pattern.compile("(?i)</?(system|instruction|admin|developer|prompt|context|question)[^>]*>"),
            Pattern.compile("(?i)\\[/?(INST|SYS)\\]")
    );

    // PII and Credential Scrubbing Patterns (LLM02)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)(?:\\+?\\d{1,3}[-.\s]?)?\\(?\\d{3}\\)?[-.\s]?\\d{3}[-.\s]?\\d{4}(?!\\d)");
    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern AWS_KEY_PATTERN = Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b");
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile("(?i)\\bBearer\\s+[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*\\b");

    // Improper Output / XSS Patterns (LLM05)
    private static final List<Pattern> UNSAFE_OUTPUT_PATTERNS = List.of(
            Pattern.compile("(?i)<script[^>]*>.*?</script>", Pattern.DOTALL),
            Pattern.compile("(?i)<script[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)onerror\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)onload\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?i)eval\\s*\\(", Pattern.CASE_INSENSITIVE)
    );

    // System Prompt Key Phrases for Leakage Detection (LLM07)
    private static final List<String> SYSTEM_PROMPT_SIGNATURES = List.of(
            "You are Anvaya-Prajna-AI",
            "pedagogical reasoning engine",
            "ReasoningProposal in valid JSON matching the schema",
            "CRITICAL INSTRUCTIONS FOR REASONING ENGINE"
    );

    /**
     * Enforces input length bounds to protect against DoS and context exhaustion (LLM08, LLM10).
     */
    public void validateInputBounds(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question cannot be null");
        }
        if (question.getStatement() != null && question.getStatement().length() > MAX_STATEMENT_LENGTH) {
            log.warn("Question statement length ({}) exceeds limit ({}). Truncating for security.",
                    question.getStatement().length(), MAX_STATEMENT_LENGTH);
            question.setStatement(question.getStatement().substring(0, MAX_STATEMENT_LENGTH));
        }
    }

    /**
     * Sanitizes question input by neutralizing prompt injections and scrubbing sensitive PII/secrets (LLM01, LLM02).
     */
    public Question sanitizeInput(Question question) {
        if (question == null) return null;

        validateInputBounds(question);

        String statement = question.getStatement();
        if (statement != null) {
            // Check for prompt injection
            if (detectPromptInjection(statement)) {
                log.warn("Prompt injection pattern detected in question {}. Neutralizing adversarial tokens.", question.getQuestionId());
                statement = neutralizePromptInjection(statement);
            }
            // Scrub sensitive PII / Credentials
            statement = scrubSensitiveData(statement);
            question.setStatement(statement);
        }

        String authoritativeAnswer = question.getAuthoritativeAnswer();
        if (authoritativeAnswer != null) {
            authoritativeAnswer = scrubSensitiveData(authoritativeAnswer);
            question.setAuthoritativeAnswer(authoritativeAnswer);
        }

        return question;
    }

    /**
     * Detects direct or indirect prompt injection patterns in input text (LLM01).
     */
    public boolean detectPromptInjection(String text) {
        if (text == null || text.isBlank()) return false;
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Neutralizes detected prompt injection patterns by redacting adversarial instructions.
     */
    public String neutralizePromptInjection(String text) {
        if (text == null) return null;
        String sanitized = text;
        for (Pattern pattern : INJECTION_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("[REDACTED_ADVERSARIAL_INSTRUCTION]");
        }
        return sanitized;
    }

    /**
     * Scrubs PII, emails, phones, SSNs, and API credentials from text (LLM02).
     */
    public String scrubSensitiveData(String text) {
        if (text == null || text.isBlank()) return text;
        String sanitized = EMAIL_PATTERN.matcher(text).replaceAll("[REDACTED_EMAIL]");
        sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll("[REDACTED_PHONE]");
        sanitized = SSN_PATTERN.matcher(sanitized).replaceAll("[REDACTED_SSN]");
        sanitized = AWS_KEY_PATTERN.matcher(sanitized).replaceAll("[REDACTED_AWS_KEY]");
        sanitized = BEARER_TOKEN_PATTERN.matcher(sanitized).replaceAll("[REDACTED_TOKEN]");
        return sanitized;
    }

    /**
     * Checks if a target model is in the approved whitelist (LLM03).
     */
    public boolean isApprovedModel(String modelName) {
        if (modelName == null) return false;
        return APPROVED_MODELS.contains(modelName.trim().toLowerCase());
    }

    /**
     * Cleans raw LLM output by removing markdown fences, neutralizing scripts, and scrubbing sensitive data (LLM05).
     */
    public String cleanAndSanitizeOutput(String rawOutput) {
        if (rawOutput == null || rawOutput.isBlank()) return null;

        String cleaned = rawOutput.trim();

        // 1. Strip Markdown Code Blocks
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline != -1) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();
        }

        // 2. Neutralize any malicious HTML / script tags
        for (Pattern pattern : UNSAFE_OUTPUT_PATTERNS) {
            cleaned = pattern.matcher(cleaned).replaceAll("");
        }

        // 3. Scrub any accidental PII or credentials in LLM output
        cleaned = scrubSensitiveData(cleaned);

        // 4. Scan for system prompt reflection / leakage
        if (containsSystemPromptLeakage(cleaned)) {
            log.warn("System prompt reflection/leakage detected in model output. Neutralizing.");
            cleaned = neutralizeSystemPromptLeakage(cleaned);
        }

        return cleaned;
    }

    /**
     * Detects if the generated output reflects internal system instructions (LLM07).
     */
    public boolean containsSystemPromptLeakage(String output) {
        if (output == null || output.isBlank()) return false;
        for (String signature : SYSTEM_PROMPT_SIGNATURES) {
            if (output.contains(signature)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Neutralizes reflected system instructions.
     */
    public String neutralizeSystemPromptLeakage(String output) {
        if (output == null) return null;
        String sanitized = output;
        for (String signature : SYSTEM_PROMPT_SIGNATURES) {
            sanitized = sanitized.replace(signature, "[CONFIDENTIAL_SYSTEM_INSTRUCTION]");
        }
        return sanitized;
    }
}
