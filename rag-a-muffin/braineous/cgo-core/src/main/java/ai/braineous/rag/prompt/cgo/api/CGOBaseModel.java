package ai.braineous.rag.prompt.cgo.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * V0 Semantic Spine Base Model.
 *
 * This class represents the minimal deterministic metadata contract
 * shared across Orchestra entities.
 *
 * Fields:
 * - id            : Unique identifier for the entity instance.
 * - version       : Version of the entity definition or contract.
 * - createdAt     : ISO-8601 UTC timestamp when created.
 * - updatedAt     : ISO-8601 UTC timestamp when last modified.
 * - status        : Execution or lifecycle status (must use stable vocabulary).
 * - correlationId : External correlation key for tracing across systems.
 * - snapshotHash  : Deterministic hash of contextual snapshot (if applicable).
 *
 * Notes:
 * - No business logic is allowed in this class.
 * - Timestamps must follow ISO-8601 UTC format.
 * - Status values must remain stable and documented elsewhere.
 *
 * This class is intentionally simple and deterministic.
 */

public class CGOBaseModel {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    private String id;

    private String createdAt;

    private String updatedAt;

    private String snapshotHash;

    public CGOBaseModel() {
    }

    //---------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSnapshotHash() {
        return snapshotHash;
    }

    public void setSnapshotHash(String snapshotHash) {
        this.snapshotHash = snapshotHash;
    }

    //---------------------------------------------
    public String toJsonString() {
        return GSON.toJson(this);
    }

    public JsonObject toJson(){
        String jsonStr = this.toJsonString();
        if(jsonStr == null || jsonStr.isBlank()){
            return null;
        }
        return JsonParser.parseString(jsonStr).getAsJsonObject();
    }

    //---------------------------------------------
    public static <T> T fromJson(String json, Class<T> type) {
        if (json == null) {
            throw new IllegalArgumentException("json cannot be null");
        }
        if (json.trim().isEmpty()) {
            throw new IllegalArgumentException("json cannot be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        return GSON.fromJson(json, type);
    }

}
