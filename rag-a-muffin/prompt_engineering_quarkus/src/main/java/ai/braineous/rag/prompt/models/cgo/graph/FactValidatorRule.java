package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;

import java.util.function.Function;

@FunctionalInterface
public interface FactValidatorRule {
    boolean validate(Fact fact, GraphView view);
}
