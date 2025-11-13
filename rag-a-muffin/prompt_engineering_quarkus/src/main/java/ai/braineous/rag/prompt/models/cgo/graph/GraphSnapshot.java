package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Edge;
import ai.braineous.rag.prompt.models.cgo.Fact;

import java.util.Collections;
import java.util.Map;

public class GraphSnapshot {

    private final Map<String, Fact> nodes;  // atomic facts
    private final Map<String, Edge> edges;  // relational facts as edges

    public GraphSnapshot(Map<String, Fact> nodes, Map<String, Edge> edges) {
        this.nodes = Collections.unmodifiableMap(nodes);
        this.edges = Collections.unmodifiableMap(edges);
    }

    public Map<String, Fact> getNodes() {
        return nodes;
    }

    public Map<String, Edge> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return "GraphSnapshot{" +
                "nodes=" + nodes +
                ", edges=" + edges +
                '}';
    }
}
