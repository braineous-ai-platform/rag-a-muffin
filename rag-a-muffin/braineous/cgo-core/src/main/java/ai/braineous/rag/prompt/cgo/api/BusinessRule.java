package ai.braineous.rag.prompt.cgo.api;

@FunctionalInterface
public interface BusinessRule {
    WorldMutation execute(GraphView view);
}
