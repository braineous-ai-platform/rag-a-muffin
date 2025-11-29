package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;

/**
 * Represents the execution of a single QueryRequest.
 *
 * Right now it's just a thin wrapper around the original request.
 * Later we can extend this with:
 *   - raw LLM payload
 *   - parsed result
 *   - timing / trace info
 *   - errors, etc.
 */
public final class QueryExecution<T extends QueryTask> {

    private final QueryRequest<T> request;

    public QueryExecution(QueryRequest<T> request) {
        this.request = request;
    }

    public QueryRequest<T> getRequest() {
        return request;
    }
}