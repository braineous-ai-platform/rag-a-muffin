package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.Node;
import com.google.gson.JsonObject;

import java.util.Map;

public final class GraphContext {

    private final Map<String, Node> nodes;

    public GraphContext(Map<String, Node> nodes) {
        this.nodes = Map.copyOf(nodes);
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public static GraphContext fromJson(JsonObject jsonObject) {

        if (jsonObject == null || !jsonObject.has("nodes") || !jsonObject.get("nodes").isJsonObject()) {
            return new GraphContext(Map.of());
        }

        JsonObject nodesObj = jsonObject.getAsJsonObject("nodes");
        Map<String, Node> nodeMap = new java.util.HashMap<>();

        for (Map.Entry<String, com.google.gson.JsonElement> entry : nodesObj.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                JsonObject nodeJson = entry.getValue().getAsJsonObject();

                // assume Node has a static factory
                Node node = Node.fromJson(nodeJson);

                if (node != null) {
                    nodeMap.put(entry.getKey(), node);
                }
            }
        }

        return new GraphContext(nodeMap);
    }

    @Override
    public String toString() {
        return "GraphContext{" +
                "nodes=" + nodes +
                '}';
    }
}

