package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Edge;
import ai.braineous.rag.prompt.models.cgo.Fact;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphBuilder {
    private final Validator validator;

    // internal mutable state
    private final Map<String, Fact> nodes = new HashMap<>(); // atomic
    private final Map<String, Edge> edges = new HashMap<>(); // relational

    public GraphBuilder(Validator validator) {
        this.validator = validator;
    }

    /**
     * Directly add/merge an atomic Fact as a node.
     * Can be used independent of bind() if needed.
     */
    public void addNode(Fact fact) {
        if (fact == null) {
            return;
        }
        upsertNode(fact);
    }

    /**
     * Validate and apply a single (from, to, edgeFact) triple.
     * On failure, graph state is unchanged.
     */
    public BindResult bind(Input input) {
        Fact from = input.getFrom();   // atomic
        Fact to   = input.getTo();     // atomic
        Fact edgeFact = input.getEdge(); // relational-as-Fact

        //make the edge relational
        edgeFact.setMode("relational");

        BindResult result = validator.bind(input);
        if (!result.isOk()) {
            return result;
        }

        //make sure from and to exist
        if(nodes.get(from.getId()) == null || nodes.get(to.getId()) == null){
            result.setOk(false);
            return result;
        }

        // upsert nodes
        upsertNode(from);
        upsertNode(to);

        // upsert edge
        upsertEdge(from, to, edgeFact);

        return result;
    }

    /**
     * Build an immutable snapshot of the current graph state.
     */
    public GraphSnapshot snapshot() {
        // copy to avoid external mutation
        Map<String, Fact> nodeCopy = new HashMap<>(nodes);
        Map<String, Edge> edgeCopy = new HashMap<>(edges);
        return new GraphSnapshot(nodeCopy, edgeCopy);
    }
    // ---------- internal helpers ----------
    private void upsertNode(Fact fact) {
        if (fact == null || fact.getId() == null) {
            return;
        }

        Fact existing = nodes.get(fact.getId());
        if (existing == null) {
            // make sure attributes is non-null
            if (fact.getAttributes() == null) {
                fact.setAttributes(new HashSet<>());
            }
            nodes.put(fact.getId(), fact);
        } else {
            // merge attributes, keep id/text/mode from existing or new as you prefer
            mergeAttributes(existing, fact);
        }
    }

    /**
     * Merge attributes from 'incoming' into 'target'.
     * Id/mode/text stay as-is on the target.
     */
    private void mergeAttributes(Fact target, Fact incoming) {
        if (incoming.getAttributes() == null) {
            return;
        }
        if (target.getAttributes() == null) {
            target.setAttributes(new HashSet<>());
        }
        target.getAttributes().addAll(incoming.getAttributes());
    }

    /**
     * Convert a relational Fact into an Edge view.
     */
    private Edge toEdge(Fact from, Fact to, Fact edgeFact) {
        Edge edge = new Edge();
        edge.setId(edgeFact.getId());
        edge.setText(edgeFact.getText());
        edge.setMode(edgeFact.getMode());

        // copy attributes defensively
        Set<String> attrs = edgeFact.getAttributes();
        if (attrs != null) {
            edge.setAttributes(new HashSet<>(attrs));
        }

        edge.setFromFactId(from.getId());
        edge.setToFactId(to.getId());

        // default score; you can tune later
        edge.setScore(1.0);

        return edge;
    }

    private void upsertEdge(Fact from, Fact to, Fact edgeFact) {
        if (edgeFact == null || edgeFact.getId() == null) {
            return;
        }

        Edge existing = edges.get(edgeFact.getId());
        if (existing == null) {
            Edge edge = toEdge(from, to, edgeFact);
            edges.put(edge.getId(), edge);
        } else {
            // merge attributes & maybe score later
            mergeAttributes(existing, edgeFact);
            // keep from/to as originally set; or assert they match
        }
    }
}
