package ai.braineous.cgo.scorer;

import ai.braineous.rag.prompt.cgo.api.QueryExecution;

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
}

