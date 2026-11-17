package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;

import java.util.List;

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
     * Execute a query through the Nexus-first cognitive path.
     */
    <T extends QueryTask> QueryExecution<T> execute(
            QueryRequest<T> request,
            Fact axis,
            List<String> relatedFacts,
            String expression,
            String definition,
            String ruleSection,
            String decisionSection);
}
