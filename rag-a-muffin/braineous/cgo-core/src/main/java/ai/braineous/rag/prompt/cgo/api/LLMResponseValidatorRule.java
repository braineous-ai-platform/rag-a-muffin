package ai.braineous.rag.prompt.cgo.api;

@FunctionalInterface
public interface LLMResponseValidatorRule {
    ValidationResult validate(String rawOutput);
}
