package ai.braineous.rag.prompt.cgo.api;

import com.google.gson.JsonObject;

public class Control {

    private String key;
    private String value;

    public Control() {
    }

    public Control(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        if (this.key != null) {
            json.addProperty("key", this.key);
        } else {
            json.add("key", null);
        }

        if (this.value != null) {
            json.addProperty("value", this.value);
        } else {
            json.add("value", null);
        }

        return json;
    }

    public static Control fromJson(JsonObject json) {
        if (json == null) {
            return null;
        }

        Control control = new Control();

        if (json.has("key") && !json.get("key").isJsonNull()) {
            control.setKey(json.get("key").getAsString());
        }

        if (json.has("value") && !json.get("value").isJsonNull()) {
            control.setValue(json.get("value").getAsString());
        }

        return control;
    }
}