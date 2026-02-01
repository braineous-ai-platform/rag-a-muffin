package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GraphSnapshot implements GraphView {

    private String id;

    private final Map<String, Fact> nodes;  // atomic facts
    private final Map<String, Edge> edges;  // relational facts as edges

    public GraphSnapshot(Map<String, Fact> nodes, Map<String, Edge> edges) {
        this.nodes = Collections.unmodifiableMap(nodes);
        this.edges = Collections.unmodifiableMap(edges);

        //generate snapshot_id
        id = "1";
    }

    public String id(){
        return this.id;
    }

    public Map<String, Fact> nodes() {
        return nodes;
    }

    public Map<String, Edge> edges() {
        return edges;
    }

    @Override
    public String toString() {
        return "GraphSnapshot{" +
                "nodes=" + nodes +
                ", edges=" + edges +
                '}';
    }

    public JsonObject toJson(){
        Gson gson = new Gson();

        JsonObject jsonObject = gson.toJsonTree(this.nodes).getAsJsonObject();

        return jsonObject;
    }

    @Override
    public Fact getFactById(String id) {
        return this.nodes.get(id);
    }

    public SnapshotHash snapshotHash() {
        SnapshotHash snapshotHash = new SnapshotHash();

        java.util.List<String> nodeIds = new java.util.ArrayList<>(this.nodes().keySet());
        java.util.List<String> edgeIds = new java.util.ArrayList<>(this.edges().keySet());
        java.util.Collections.sort(nodeIds);
        java.util.Collections.sort(edgeIds);

        String payload = String.join("|", nodeIds) + "||" + String.join("|", edgeIds);

        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));

            snapshotHash.setValue(sb.toString());

            return snapshotHash;
        } catch (Exception e) {
            throw new RuntimeException("snapshot_hash_failed: " + e.getMessage(), e);
        }
    }

    public boolean doesFactExist(Fact fact){
        if(fact == null){
            return false;
        }

        String factId = fact.getId();
        if(factId == null || factId.trim().length()==0){
            return false;
        }

        if(!fact.getMode().equals("atomic")){
            return false;
        }

        for(var entry: this.nodes.entrySet()){
            Fact local = entry.getValue();
            if(local.getId().trim().equals(factId)){
                return true;
            }
        }


        return false;
    }

    //--------------------------------------
    public Fact findFact(String factId) {

        if (factId == null) {
            return null;
        }

        String wanted = factId.trim();
        if (wanted.isEmpty()) {
            return null;
        }

        if (this.nodes == null || this.nodes.isEmpty()) {
            return null;
        }

        for (var entry : this.nodes.entrySet()) {
            Fact fact = entry.getValue();
            if (fact == null) {
                continue;
            }

            String id = fact.getId();
            if (id == null) {
                continue;
            }

            if (id.trim().equals(wanted)) {
                return fact;
            }
        }

        return null;
    }


    public List<Fact> findNeighbors(String factId) {

        List<Fact> facts = new ArrayList<>();

        if (factId == null) {
            return facts;
        }

        String wanted = factId.trim();
        if (wanted.isEmpty()) {
            return facts;
        }

        Fact anchor = this.findFact(wanted);
        if (anchor == null) {
            return facts;
        }

        String anchorId = anchor.getId();
        if (anchorId == null) {
            return facts;
        }

        anchorId = anchorId.trim();
        if (anchorId.isEmpty()) {
            return facts;
        }

        if (this.edges == null || this.edges.isEmpty()) {
            return facts;
        }

        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<String>();

        for (var entry : this.edges.entrySet()) {

            Edge edge = entry.getValue();
            if (edge == null) {
                continue;
            }

            String fromId = edge.getFromFactId();
            String toId = edge.getToFactId();

            if (fromId == null || toId == null) {
                continue;
            }

            String from = fromId.trim();
            if (from.isEmpty()) {
                continue;
            }

            if (!from.equals(anchorId)) {
                continue;
            }

            String to = toId.trim();
            if (to.isEmpty()) {
                continue;
            }

            if (seen.contains(to)) {
                continue;
            }

            Fact neighbor = this.findFact(to);
            if (neighbor == null) {
                continue;
            }

            String neighborId = neighbor.getId();
            if (neighborId == null) {
                continue;
            }

            if (neighborId.trim().isEmpty()) {
                continue;
            }

            seen.add(to);
            facts.add(neighbor);
        }

        return facts;
    }
}
