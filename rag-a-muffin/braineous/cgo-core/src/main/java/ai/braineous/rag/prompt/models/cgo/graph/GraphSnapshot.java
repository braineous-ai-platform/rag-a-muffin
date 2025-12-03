package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Collections;
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
}
