package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryRequest;

public final class QueryRequests {

    private QueryRequests() {
    }

    public static QueryRequest<ValidateTask> validateTask(
            Meta meta,
            ValidateTask task,
            GraphContext context,
            String factId
    ) {
        return new QueryRequest<>(meta, context, task);
    }
}

