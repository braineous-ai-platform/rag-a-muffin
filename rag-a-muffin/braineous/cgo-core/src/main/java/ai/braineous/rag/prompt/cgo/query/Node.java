package ai.braineous.rag.prompt.cgo.query;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class Node {

    public enum Mode {
        ATOMIC,
        RELATIONAL
    }

    private final String id;          // e.g. "Flight:F100"
    private final String text;        // JSON payload as string
    private final List<String> attributes;
    private final Mode mode;

    public Node(String id, String text, List<String> attributes, Mode mode) {
        this.id = id;
        this.text = text;
        this.attributes = List.copyOf(attributes);
        this.mode = mode;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public Mode getMode() {
        return mode;
    }

    public static Node fromJson(JsonObject jsonObject) {

        if (jsonObject == null) {
            return null;
        }

        String id = jsonObject.has("id")
                ? jsonObject.get("id").getAsString()
                : null;

        String text = jsonObject.has("text")
                ? jsonObject.get("text").getAsString()
                : null;

        // attributes (optional, default empty)
        List<String> attributes = new ArrayList<>();
        if (jsonObject.has("attributes") && jsonObject.get("attributes").isJsonArray()) {
            JsonArray attrArray = jsonObject.getAsJsonArray("attributes");
            for (JsonElement elem : attrArray) {
                if (elem.isJsonPrimitive()) {
                    attributes.add(elem.getAsString());
                }
            }
        }

        // mode (default RELATIONAL if missing / unknown)
        Mode mode = Mode.RELATIONAL;
        if (jsonObject.has("mode")) {
            try {
                mode = Mode.valueOf(jsonObject.get("mode").getAsString().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // keep default
            }
        }

        return new Node(id, text, attributes, mode);
    }
}
