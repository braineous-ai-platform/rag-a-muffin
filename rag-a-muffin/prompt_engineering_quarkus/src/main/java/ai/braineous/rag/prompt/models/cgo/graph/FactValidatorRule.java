package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;

@FunctionalInterface
public interface FactValidatorRule {
    boolean validate(Fact fact, GraphView view);
}
