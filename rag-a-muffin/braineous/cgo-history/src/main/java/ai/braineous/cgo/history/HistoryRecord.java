package ai.braineous.cgo.history;

import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Immutable wrapper around a single QueryExecution.
 *
 * This is the atomic unit for query history:
 *  - Append one HistoryRecord per QueryExecution.
 *  - Scorer can later read the underlying QueryExecution and
 *    derive whatever it needs (meta, queryKind, validations, etc.).
 *
 * Storage (Mongo) and higher-level scoring logic sit on top of this.
 */
public final class HistoryRecord {

    private final QueryExecution<?> queryExecution;
    private final ScorerResult result;

    public HistoryRecord(QueryExecution<?> queryExecution, ScorerResult result) {
        this.queryExecution = queryExecution;
        this.result = result;
    }

    public QueryExecution<?> getQueryExecution() {
        return queryExecution;
    }

    public ScorerResult getResult() {
        return result;
    }

    /**
     * Convenience: expose queryKind via Meta, if QueryExecution → Request → Meta is wired.
     * Adjust this if your API shape differs.
     */
    public String getQueryKind() {
        return queryExecution
                .getRequest()
                .getMeta()
                .getQueryKind();
    }

    /**
     * Convenience: expose Meta version (useful for evolution / scoring).
     */
    public String getVersion() {
        return queryExecution
                .getRequest()
                .getMeta()
                .getVersion();
    }

    @Override
    public String toString() {
        return "HistoryRecord{" +
                "queryExecution=" + queryExecution +
                ", result=" + result +
                '}';
    }

    //---------------------------------------------------------
    public com.google.gson.JsonObject toJson() {

        com.google.gson.JsonObject out = new com.google.gson.JsonObject();

        if (this.queryExecution != null) {
            out.add("queryExecution", this.queryExecution.toJson());
        } else {
            out.add("queryExecution", null);
        }

        if (this.result != null) {
            out.add("result", this.result.toJson());
        } else {
            out.add("result", null);
        }

        return out;
    }

    public static HistoryRecord fromJson(com.google.gson.JsonObject json) {

        if (json == null) {
            throw new IllegalArgumentException("HistoryRecord JSON cannot be null");
        }

        ai.braineous.rag.prompt.cgo.api.QueryExecution<?> queryExecution = null;
        if (json.has("queryExecution") && !json.get("queryExecution").isJsonNull()) {
            queryExecution = ai.braineous.rag.prompt.cgo.api.QueryExecution.fromJson(
                    json.getAsJsonObject("queryExecution")
            );
        }

        ScorerResult result = null;
        if (json.has("result") && !json.get("result").isJsonNull()) {
            result = ScorerResult.fromJson(
                    json.getAsJsonObject("result")
            );
        }

        return new HistoryRecord(queryExecution, result);
    }


}

