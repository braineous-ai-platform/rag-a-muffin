package ai.braineous.rag.prompt.cgo.api;

@FunctionalInterface
public interface FactValidatorRule {
    boolean validate(Fact fact, GraphView view);
}
