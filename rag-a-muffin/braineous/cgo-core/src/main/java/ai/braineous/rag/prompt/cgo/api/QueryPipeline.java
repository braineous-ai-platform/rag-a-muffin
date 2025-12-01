package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;

/**
 * API-level entry point for executing CGO queries.
 *
 * This lives in the api package so that callers only depend on:
 *   - QueryRequest<T>
 *   - QueryTask
 *   - QueryExecution<T>
 */
public interface QueryPipeline {

    /**
     * Execute a query request.
     *
     * For now we return a minimal QueryExecution wrapper.
     * The response/result shape will be designed in the "response phase" later.
     */
    <T extends QueryTask> QueryExecution<T> execute(QueryRequest<T> request);
}

