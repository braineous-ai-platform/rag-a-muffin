package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.cgo.config.CGOSystemConfig;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import com.mongodb.client.MongoClient;

import java.util.*;

public class GraphStoreImpl implements GraphStore{
    private static final GraphStoreImpl store = new GraphStoreImpl();

    private final Map<String, Fact> nodes = new HashMap<>(); // atomic
    private final Map<String, Edge> edges = new HashMap<>(); // relational

    //MongoStore
    private MongoClient mongoClient;
    private GraphStoreMongo mongoStore;

    private GraphStoreImpl() {
        String mongoDbUri = CGOSystemConfig.resolveMongoDBUri();
        mongoClient = com.mongodb.client.MongoClients.create(mongoDbUri);
        mongoStore = new GraphStoreMongo(mongoClient);
    }

    public static GraphStoreImpl getInstance(){
        return store;
    }
    //---------------------------------------------------------
    /**
     * Build an immutable snapshot of the current graph state.
     */
    public GraphSnapshot snapshot() {
        /*Map<String, Fact> nodeCopy = new HashMap<>(nodes);
        Map<String, Edge> edgeCopy = new HashMap<>(edges);
        return new GraphSnapshot(
                java.util.Collections.unmodifiableMap(nodeCopy),
                java.util.Collections.unmodifiableMap(edgeCopy)
        );*/


        return mongoStore.snapshot();
    }


    //public Map<String, Fact> nodes() {
    //    return nodes;
    //}

    //public Map<String, Edge> edges() {
    //    return edges;
    //}

    void clear(){
        this.nodes.clear();
        this.edges.clear();
    }

    public void upsertNode(Fact fact) {
        /*if (fact == null || fact.getId() == null) {
            return;
        }

        Fact existing = nodes.get(fact.getId());

        if (existing == null) {
            // defensive copy for attrs
            if (fact.getAttributes() != null) {
                fact.setAttributes(new HashSet<>(fact.getAttributes()));
            } else {
                fact.setAttributes(new HashSet<>());
            }
            nodes.put(fact.getId(), fact);
            return;
        }

        // ✅ UPDATE semantics: overwrite payload if provided
        if (fact.getText() != null) {
            existing.setText(fact.getText());
        }

        // optional: if you want mode updates (usually keep stable)
        // if (fact.getMode() != null) existing.setMode(fact.getMode());

        // ✅ always merge attributes
        mergeAttributes(existing, fact);*/


        mongoStore.upsertNode(fact);
    }


    public void deleteNode(Fact fact){
        /*if (fact == null || fact.getId() == null) {
            return;
        }

        //remove edges where this fact applies
        Set<Edge> remove = new HashSet<>();
        for (var entry : this.edges.entrySet()) {
            Edge edge = entry.getValue();
            if (edge == null) continue;

            String fromId = edge.getFromFactId();
            String toId = edge.getToFactId();

            if (fact.getId().equals(fromId) || fact.getId().equals(toId)) {
                remove.add(edge);
            }
        }
        for(Edge edge: remove){
            this.edges.remove(edge.getId());
        }

        nodes.remove(fact.getId());*/


        mongoStore.deleteNode(fact);
    }

    public void mutate(Fact from, Fact to, Fact edgeFact){
        /*if (from == null || from.getId() == null) return;
        if (to == null || to.getId() == null) return;
        if (edgeFact == null || edgeFact.getId() == null) return;

        this.upsertNode(from);
        this.upsertNode(to);
        this.upsertEdge(from, to, edgeFact);*/


        mongoStore.mutate(from, to, edgeFact);

    }
    // ---------- internal helpers ----------

    /**
     * Merge attributes from 'incoming' into 'target'.
     * Id/mode/text stay as-is on the target.
     */
    private void mergeAttributes(Fact target, Fact incoming) {
        if (target == null || incoming == null) return;
        if (incoming.getAttributes() == null) return;

        if (target.getAttributes() == null) {
            target.setAttributes(new HashSet<>());
        }
        target.getAttributes().addAll(incoming.getAttributes());
    }


    /**
     * Convert a relational Fact into an Edge view.
     */
    private Edge toEdge(Fact from, Fact to, Fact edgeFact) {
        if (from == null || from.getId() == null) return null;
        if (to == null || to.getId() == null) return null;
        if (edgeFact == null || edgeFact.getId() == null) return null;

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
            if (edge == null) return;
            edges.put(edge.getId(), edge);
        } else {
            // merge attributes & maybe score later
            mergeAttributes(existing, edgeFact);
            // keep from/to as originally set; or assert they match
        }
    }
}
