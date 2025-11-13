package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Edge;
import ai.braineous.rag.prompt.models.cgo.Fact;

import java.util.HashMap;
import java.util.Map;

public class GraphBuilder {
    private final Validator validator;

    // internal mutable state
    private final Map<String, Fact> nodes = new HashMap<>(); // atomic
    private final Map<String, Edge> edges = new HashMap<>(); // relational

    public GraphBuilder(Validator validator) {
        this.validator = validator;
    }

    /**
     * Validate and apply a single (from, to, edgeFact) triple.
     * On failure, graph state is unchanged.
     */
    public BindResult bind(Input input) {
        return null;
    }

    /**
     * Directly add/merge an atomic Fact as a node.
     * Can be used independent of bind() if needed.
     */
    public void addNode(Fact fact) {

    }

    /**
     * Build an immutable snapshot of the current graph state.
     */
    public GraphSnapshot snapshot() {
        return null;
    }

    // ---------- internal helpers ----------
}
