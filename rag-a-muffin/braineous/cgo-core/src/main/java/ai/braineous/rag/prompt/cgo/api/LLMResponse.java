package ai.braineous.rag.prompt.cgo.api;

import com.google.gson.JsonObject;

public class LLMResponse extends CGOBaseModel {

    private LLMRequest llmRequest;
    private String rawResponse;
    private boolean success;

    public LLMResponse() {
    }

    public LLMRequest getLlmRequest() {
        return llmRequest;
    }

    public void setLlmRequest(LLMRequest llmRequest) {
        this.llmRequest = llmRequest;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = new JsonObject();

        if (this.llmRequest != null) {
            out.add("llmRequest", this.llmRequest.toJson());
        } else {
            out.add("llmRequest", null);
        }

        if (this.rawResponse != null) {
            out.addProperty("rawResponse", this.rawResponse);
        } else {
            out.add("rawResponse", null);
        }

        out.addProperty("success", this.success);

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

    public static LLMResponse fromJson(JsonObject json) {
        if (json == null) {
            throw new IllegalArgumentException("LLMResponse JSON cannot be null");
        }

        LLMResponse out = new LLMResponse();

        if (json.has("llmRequest") && !json.get("llmRequest").isJsonNull()) {
            out.setLlmRequest(LLMRequest.fromJson(json.getAsJsonObject("llmRequest")));
        }

        if (json.has("rawResponse") && !json.get("rawResponse").isJsonNull()) {
            out.setRawResponse(json.get("rawResponse").getAsString());
        }

        if (json.has("success") && !json.get("success").isJsonNull()) {
            out.setSuccess(json.get("success").getAsBoolean());
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