package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;

@FunctionalInterface
public interface BusinessRule {
    WorldMutation execute(GraphView view);
}
