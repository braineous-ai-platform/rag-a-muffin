package ai.braineous.rag.prompt.models;

import com.google.gson.JsonObject;

public class Property {
    private JsonObject value;

    public Property(){

    }

    public JsonObject getValue() {
        return value;
    }

    public void setValue(JsonObject value) {
        this.value = value;
    }
}
