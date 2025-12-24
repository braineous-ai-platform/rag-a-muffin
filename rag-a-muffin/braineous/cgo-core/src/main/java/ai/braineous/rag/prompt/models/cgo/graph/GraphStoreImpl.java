package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;

import java.util.*;

public class GraphStoreImpl implements GraphStore{
    private static GraphStoreImpl store = new GraphStoreImpl();

    private final Map<String, Fact> nodes = new HashMap<>(); // atomic
    private final Map<String, Edge> edges = new HashMap<>(); // relational

    private GraphStoreImpl() {
    }

    public static GraphStoreImpl getInstance(){
        return GraphStoreImpl.store;
    }
    //---------------------------------------------------------
    /**
     * Build an immutable snapshot of the current graph state.
     */
    public GraphSnapshot snapshot() {
        // copy to avoid external mutation
        Map<String, Fact> nodeCopy = new HashMap<>(nodes);
        Map<String, Edge> edgeCopy = new HashMap<>(edges);
        return new GraphSnapshot(nodeCopy, edgeCopy);
    }

    public Map<String, Fact> nodes() {
        return nodes;
    }

    public Map<String, Edge> edges() {
        return edges;
    }

    public void clear(){
        this.nodes.clear();
        this.edges.clear();
    }

    public void upsertNode(Fact fact) {
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

    public void mutate(Fact from, Fact to, Fact edgeFact){
        // upsert nodes
        store.upsertNode(from);
        store.upsertNode(to);

        // upsert edge
        store.upsertEdge(from, to, edgeFact);
    }
    // ---------- internal helpers ----------

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
