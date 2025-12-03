package ai.braineous.rag.prompt.models.cgo.query;

import com.google.gson.JsonObject;

public class Query {

    private JsonObject queryJson;

    public Query() {
    }

    public Query(JsonObject queryJson) {
        this.queryJson = queryJson;
    }

    public JsonObject getQueryJson() {
        return queryJson;
    }

    public void setQueryJson(JsonObject queryJson) {
        this.queryJson = queryJson;
    }
}
