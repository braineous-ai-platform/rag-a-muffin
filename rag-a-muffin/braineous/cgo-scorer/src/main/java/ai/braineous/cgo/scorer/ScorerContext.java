package ai.braineous.cgo.scorer;

import ai.braineous.rag.prompt.cgo.api.QueryExecution;

public class ScorerContext {

    private QueryExecution queryExecution;

    public ScorerContext(QueryExecution queryExecution) {
        this.queryExecution = queryExecution;
    }

    public QueryExecution getQueryExecution() {
        return queryExecution;
    }

    public void setQueryExecution(QueryExecution queryExecution) {
        this.queryExecution = queryExecution;
    }

    @Override
    public String toString() {
        return "ScorerContext{" +
                "queryExecution=" + queryExecution +
                '}';
    }
}
