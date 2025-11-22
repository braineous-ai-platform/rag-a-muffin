package ai.braineous.rag.prompt.models.cgo.graph;

@FunctionalInterface
public interface BusinessRule {
    Proposal execute(GraphView view);
}
