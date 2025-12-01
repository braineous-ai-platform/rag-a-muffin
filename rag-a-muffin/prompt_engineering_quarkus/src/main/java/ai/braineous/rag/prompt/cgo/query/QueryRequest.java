package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.LLMResponseValidatorRule;
import ai.braineous.rag.prompt.cgo.api.Meta;

import java.util.Objects;

/**
 * API-level request submitted to QueryPipeline.
 *
 * This is independent of how we later turn it into an LLM prompt
 * (response contracts, instructions, etc).
 */
public final class QueryRequest<T extends QueryTask> {

    private final Meta meta;
    private final GraphContext context;
    private final T task;

    private LLMResponseValidatorRule rule;

    public QueryRequest(Meta meta, GraphContext context, T task) {
        this.meta = Objects.requireNonNull(meta, "meta must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");
        this.task = Objects.requireNonNull(task, "task must not be null");
    }

    public QueryRequest(Meta meta, GraphContext context, T task, LLMResponseValidatorRule rule) {
        this.meta = meta;
        this.context = context;
        this.task = task;
        this.rule = rule;
    }

    public Meta getMeta() {
        return meta;
    }

    public GraphContext getContext() {
        return context;
    }

    public T getTask() {
        return task;
    }

    public LLMResponseValidatorRule getRule() {
        return rule;
    }
}

