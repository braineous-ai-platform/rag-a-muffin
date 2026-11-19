package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;

import java.util.List;

/**
 * Simple test double for QueryPipeline.
 * It just captures the last request and wraps it in a QueryExecution.
 */
final class FakeQueryPipeline implements QueryPipeline {

    private QueryRequest<?> lastRequest;

    @Override
    public <T extends QueryTask> QueryExecution<T> execute(
            QueryRequest<T> request,
            Fact axis,
            List<String> relatedFacts,
            String expression,
            String definition,
            String ruleSection,
            String decisionSection) {
        this.lastRequest = request;
        return new QueryExecution<>(request);
    }

    @Override
    public <T extends QueryTask> String executeDeveloper(
            QueryRequest<T> request,
            Fact axis,
            List<String> relatedFacts,
            String expression,
            String definition,
            String ruleSection,
            String decisionSection) {
        return this.execute(
                request,
                axis,
                relatedFacts,
                expression,
                definition,
                ruleSection,
                decisionSection).getDeveloperResponse();
    }

    public QueryRequest<?> getLastRequest() {
        return lastRequest;
    }
}
