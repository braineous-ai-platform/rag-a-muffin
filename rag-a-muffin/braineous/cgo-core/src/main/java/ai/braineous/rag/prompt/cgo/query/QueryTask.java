package ai.braineous.rag.prompt.cgo.query;

import com.google.gson.JsonObject;

/**
 * Marker/base interface for all query tasks.
 * Each queryKind will have its own concrete implementation.
 */
public interface QueryTask {
    String getDescription();

    public JsonObject toJson();
}

