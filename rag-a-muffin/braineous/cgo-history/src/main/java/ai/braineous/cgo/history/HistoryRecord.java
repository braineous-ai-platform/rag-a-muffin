package ai.braineous.cgo.history;

import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import com.google.gson.JsonObject;

import java.time.Instant;
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

    private HistoryStatus status;

    private String approvedCommitId;

    private Instant createdAt;

    private Instant updatedAt;

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

    //--------------------------------------------------------------------
    public HistoryStatus getStatus() {
        return this.status;
    }

    public String getApprovedCommitId() {
        return this.approvedCommitId;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void markPending(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now cannot be null");
        }
        this.status = HistoryStatus.PENDING;
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    public void markAccepted(String commitId, Instant now) {
        if (commitId == null || commitId.trim().isEmpty()) {
            throw new IllegalArgumentException("commitId cannot be null/empty");
        }
        if (now == null) {
            throw new IllegalArgumentException("now cannot be null");
        }
        this.status = HistoryStatus.ACCEPTED;
        this.approvedCommitId = commitId;
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    //--------------------------------------------------------------------

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

        if (this.status != null) {
            out.addProperty("status", this.status.name());
        } else {
            out.add("status", null);
        }

        if (this.approvedCommitId != null) {
            out.addProperty("approvedCommitId", this.approvedCommitId);
        } else {
            out.add("approvedCommitId", null);
        }

        if (this.createdAt != null) {
            out.addProperty("createdAt", this.createdAt.toString());
        } else {
            out.add("createdAt", null);
        }

        if (this.updatedAt != null) {
            out.addProperty("updatedAt", this.updatedAt.toString());
        } else {
            out.add("updatedAt", null);
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

        HistoryRecord r = new HistoryRecord(queryExecution, result);

        // ---- status ----
        if (json.has("status") && !json.get("status").isJsonNull()) {
            String s = null;
            try {
                s = json.get("status").getAsString();
            } catch (RuntimeException e) {
                s = null;
            }

            if (s != null) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        r.status = HistoryStatus.valueOf(trimmed);
                    } catch (RuntimeException e) {
                        // unknown status -> ignore for forward compatibility
                        r.status = null;
                    }
                }
            }
        }

        // ---- approvedCommitId ----
        if (json.has("approvedCommitId") && !json.get("approvedCommitId").isJsonNull()) {
            String cid = null;
            try {
                cid = json.get("approvedCommitId").getAsString();
            } catch (RuntimeException e) {
                cid = null;
            }

            if (cid != null) {
                String trimmed = cid.trim();
                if (!trimmed.isEmpty()) {
                    r.approvedCommitId = trimmed;
                }
            }
        }

        // ---- createdAt ----
        if (json.has("createdAt") && !json.get("createdAt").isJsonNull()) {
            String t = null;
            try {
                t = json.get("createdAt").getAsString();
            } catch (RuntimeException e) {
                t = null;
            }

            if (t != null) {
                String trimmed = t.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        r.createdAt = Instant.parse(trimmed);
                    } catch (RuntimeException e) {
                        r.createdAt = null;
                    }
                }
            }
        }

        // ---- updatedAt ----
        if (json.has("updatedAt") && !json.get("updatedAt").isJsonNull()) {
            String t = null;
            try {
                t = json.get("updatedAt").getAsString();
            } catch (RuntimeException e) {
                t = null;
            }

            if (t != null) {
                String trimmed = t.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        r.updatedAt = Instant.parse(trimmed);
                    } catch (RuntimeException e) {
                        r.updatedAt = null;
                    }
                }
            }
        }

        return r;
    }

}

