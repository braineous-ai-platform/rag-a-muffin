package ai.braineous.rag.prompt.cgo.api;

import com.google.gson.JsonObject;

public class ResponseContract {

    private String type;
    private String description;
    private JsonObject schema; // keep flexible for now

    public ResponseContract() {}

    public ResponseContract(String type, String description, JsonObject schema) {
        this.type = type;
        this.description = description;
        this.schema = schema;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JsonObject getSchema() {
        return schema;
    }

    public void setSchema(JsonObject schema) {
        this.schema = schema;
    }

    // Optional helper: build from the "response_contract" JsonObject
    public static ResponseContract fromJson(JsonObject rcObj) {
        if (rcObj == null) return null;

        String type = rcObj.has("type") ? rcObj.get("type").getAsString() : null;
        String description = rcObj.has("description") ? rcObj.get("description").getAsString() : null;

        JsonObject schema = rcObj.has("schema") && rcObj.get("schema").isJsonObject()
                ? rcObj.getAsJsonObject("schema")
                : null;

        return new ResponseContract(type, description, schema);
    }
}