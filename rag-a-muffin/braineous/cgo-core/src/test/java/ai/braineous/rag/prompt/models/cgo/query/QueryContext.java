package ai.braineous.rag.prompt.models.cgo.query;

public class QueryContext {
    private Query query;

    public QueryContext() {
    }

    public QueryContext(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
}
