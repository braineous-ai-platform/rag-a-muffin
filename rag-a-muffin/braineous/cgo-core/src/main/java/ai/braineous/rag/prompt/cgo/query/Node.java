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

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", attributes=" + attributes +
                ", mode=" + mode +
                '}';
    }

    //--------------------------------------
    public JsonObject toJson() {

        JsonObject out = new JsonObject();

        if (this.id != null) {
            out.addProperty("id", this.id);
        } else {
            out.add("id", null);
        }

        if (this.text != null) {
            out.addProperty("text", this.text);
        } else {
            out.add("text", null);
        }

        JsonArray attrs = new JsonArray();
        for (String attr : this.attributes) {
            if (attr != null) {
                attrs.add(attr);
            }
        }
        out.add("attributes", attrs);

        if (this.mode != null) {
            out.addProperty("mode", this.mode.name());
        } else {
            out.add("mode", null);
        }

        return out;
    }

    public String toJsonString() {
        return toJson().toString();
    }

    public static Node fromJson(JsonObject jsonObject) {

        if (jsonObject == null) {
            return null;
        }

        String id = null;
        if (jsonObject.has("id") && !jsonObject.get("id").isJsonNull()) {
            id = jsonObject.get("id").getAsString();
        }

        String text = null;
        if (jsonObject.has("text") && !jsonObject.get("text").isJsonNull()) {
            text = jsonObject.get("text").getAsString();
        }

        List<String> attributes = new ArrayList<>();
        if (jsonObject.has("attributes") && jsonObject.get("attributes").isJsonArray()) {
            JsonArray attrArray = jsonObject.getAsJsonArray("attributes");
            for (JsonElement elem : attrArray) {
                if (elem.isJsonPrimitive()) {
                    attributes.add(elem.getAsString());
                }
            }
        }

        Mode mode = Mode.RELATIONAL;
        if (jsonObject.has("mode") && !jsonObject.get("mode").isJsonNull()) {
            try {
                mode = Mode.valueOf(jsonObject.get("mode").getAsString());
            } catch (IllegalArgumentException ignored) {
                // keep default
            }
        }

        return new Node(id, text, attributes, mode);
    }


}
