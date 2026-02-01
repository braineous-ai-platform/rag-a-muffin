package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.Node;
import com.google.gson.JsonObject;

import java.util.*;

public final class GraphContext {

    private Map<String, Node> nodes;

    public GraphContext() {
        this.nodes = new HashMap<>();
    }

    public GraphContext(Map<String, Node> nodes) {
        this.nodes = Map.copyOf(nodes);
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "GraphContext{" +
                "nodes=" + nodes +
                '}';
    }

    //---------------------------------------


    //------------------------------------
    public JsonObject toJson() {

        JsonObject out = new JsonObject();
        JsonObject nodesObj = new JsonObject();

        for (Map.Entry<String, Node> entry : this.nodes.entrySet()) {

            String key = entry.getKey();
            Node node = entry.getValue();

            if (key != null && node != null) {
                nodesObj.add(key, node.toJson());
            }
        }

        out.add("nodes", nodesObj);
        return out;
    }

    public String toJsonString() {
        return toJson().toString();
    }

    public static GraphContext fromJson(JsonObject jsonObject) {

        if (jsonObject == null) {
            return new GraphContext(Map.of());
        }

        if (!jsonObject.has("nodes") || !jsonObject.get("nodes").isJsonObject()) {
            return new GraphContext(Map.of());
        }

        JsonObject nodesObj = jsonObject.getAsJsonObject("nodes");
        Map<String, Node> nodeMap = new java.util.HashMap<>();

        for (Map.Entry<String, com.google.gson.JsonElement> entry : nodesObj.entrySet()) {

            if (!entry.getValue().isJsonObject()) {
                continue;
            }

            JsonObject nodeJson = entry.getValue().getAsJsonObject();
            Node node = Node.fromJson(nodeJson);

            if (node != null) {
                nodeMap.put(entry.getKey(), node);
            }
        }

        return new GraphContext(nodeMap);
    }

    //---------------------------------
    public void addFact(Fact fact) {

        if (fact == null) {
            return;
        }

        String id = fact.getId();
        if (id == null) {
            return;
        }
        id = id.trim();
        if (id.isEmpty()) {
            return;
        }

        if (this.nodes == null) {
            return;
        }

        String text = fact.getText();
        if (text == null) {
            text = "";
        }

        Node.Mode mode = Node.Mode.ATOMIC;

        Node node = new Node(
                id,
                text,
                new ArrayList<>(),
                mode
        );

        this.nodes.put(id, node);
    }


}

