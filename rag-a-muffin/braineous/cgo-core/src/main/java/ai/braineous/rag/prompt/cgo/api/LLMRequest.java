package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import com.google.gson.JsonObject;

public class LLMRequest extends CGOBaseModel {

    private QueryRequest queryRequest;

    private JsonObject llmQuery;

    public LLMRequest() {
    }

    public QueryRequest getQueryRequest() {
        return queryRequest;
    }

    public void setQueryRequest(QueryRequest queryRequest) {
        this.queryRequest = queryRequest;
    }

    public JsonObject getLlmQuery() {
        return llmQuery;
    }

    public void setLlmQuery(JsonObject llmQuery) {
        this.llmQuery = llmQuery;
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = new JsonObject();

        if (this.queryRequest != null) {
            out.add("queryRequest", this.queryRequest.toJson());
        } else {
            out.add("queryRequest", null);
        }

        if (this.llmQuery != null) {
            out.add("llmQuery", this.llmQuery);
        } else {
            out.add("llmQuery", null);
        }

        if (this.getId() != null) {
            out.addProperty("id", this.getId());
        } else {
            out.add("id", null);
        }

        if (this.getCreatedAt() != null) {
            out.addProperty("createdAt", this.getCreatedAt());
        } else {
            out.add("createdAt", null);
        }

        if (this.getUpdatedAt() != null) {
            out.addProperty("updatedAt", this.getUpdatedAt());
        } else {
            out.add("updatedAt", null);
        }

        if (this.getSnapshotHash() != null) {
            out.addProperty("snapshotHash", this.getSnapshotHash());
        } else {
            out.add("snapshotHash", null);
        }

        return out;
    }

    public static LLMRequest fromJson(JsonObject json) {
        if (json == null) {
            throw new IllegalArgumentException("LLMRequest JSON cannot be null");
        }

        LLMRequest out = new LLMRequest();

        if (json.has("queryRequest") && !json.get("queryRequest").isJsonNull()) {
            out.setQueryRequest(QueryRequest.fromJson(json.getAsJsonObject("queryRequest")));
        }

        if (json.has("llmQuery") && !json.get("llmQuery").isJsonNull()) {
            out.setLlmQuery(json.getAsJsonObject("llmQuery"));
        }

        if (json.has("id") && !json.get("id").isJsonNull()) {
            out.setId(json.get("id").getAsString());
        }

        if (json.has("createdAt") && !json.get("createdAt").isJsonNull()) {
            out.setCreatedAt(json.get("createdAt").getAsString());
        }

        if (json.has("updatedAt") && !json.get("updatedAt").isJsonNull()) {
            out.setUpdatedAt(json.get("updatedAt").getAsString());
        }

        if (json.has("snapshotHash") && !json.get("snapshotHash").isJsonNull()) {
            out.setSnapshotHash(json.get("snapshotHash").getAsString());
        }

        return out;
    }
}