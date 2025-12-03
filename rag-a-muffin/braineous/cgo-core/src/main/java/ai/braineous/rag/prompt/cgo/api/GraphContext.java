package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.Node;

import java.util.Map;

public final class GraphContext {

    private final Map<String, Node> nodes;

    public GraphContext(Map<String, Node> nodes) {
        this.nodes = Map.copyOf(nodes);
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }
}

