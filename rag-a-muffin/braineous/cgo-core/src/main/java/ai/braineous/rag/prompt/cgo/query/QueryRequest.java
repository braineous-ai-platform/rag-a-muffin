package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.LLMResponseValidatorRule;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.LlmAdapter;
import com.google.gson.JsonObject;

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

    private String factId;

    private LLMResponseValidatorRule rule;

    private LlmAdapter adapter;

    public QueryRequest(Meta meta, GraphContext context, T task) {
        this.meta = Objects.requireNonNull(meta, "meta must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");
        this.task = Objects.requireNonNull(task, "task must not be null");
    }


    public QueryRequest(Meta meta, GraphContext context, T task, String factId) {
        this.meta = Objects.requireNonNull(meta, "meta must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");
        this.task = Objects.requireNonNull(task, "task must not be null");
        this.factId = factId;
    }

    public QueryRequest(Meta meta, GraphContext context, T task, LLMResponseValidatorRule rule) {
        this.meta = meta;
        this.context = context;
        this.task = task;
        this.rule = rule;
    }

    public QueryRequest(Meta meta, GraphContext context, T task, String factId, LLMResponseValidatorRule rule) {
        this.meta = meta;
        this.context = context;
        this.task = task;
        this.factId = factId;
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

    public LlmAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(LlmAdapter adapter) {
        this.adapter = adapter;
    }

    public String getFactId() {
        return factId;
    }

    public void setFactId(String factId) {
        this.factId = factId;
    }

    public String safeQueryKind() {
        if (this.meta == null) {
            return null;
        }
        return safe(this.meta.getQueryKind());
    }

    public String safeFactId() {
        if (this.factId == null || this.factId.trim().isBlank()) {
            return null;
        }
        return safe(this.factId);
    }
    private static String safe(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }


    @Override
    public String toString() {
        String adapterType = (adapter == null) ? "null" : adapter.getClass().getSimpleName();
        return "QueryRequest{" +
                "meta=" + meta +
                ", factId = " + this.factId +
                ", context=" + context +
                ", task=" + task +
                ", rule=" + rule +
                ", adapterType=" + adapterType +
                '}';
    }

    //---------------------------------------------------------
    public JsonObject toJson() {

        JsonObject out = new JsonObject();

        out.addProperty("factId", this.factId);

        // ---- meta ----
        out.add("meta", this.meta.toJson());

        // ---- context ----
        out.add("context", this.context.toJson());

        // ---- task ----
        out.add("task", this.task.toJson());
        out.addProperty("taskType", this.task.getClass().getName());

        // ---- rule (optional, identity only) ----
        if (this.rule != null) {
            out.addProperty("rule", this.rule.getClass().getName());
        } else {
            out.add("rule", null);
        }

        // ---- adapter (optional, identity only) ----
        if (this.adapter != null) {
            out.addProperty("adapter", this.adapter.getClass().getName());
        } else {
            out.add("adapter", null);
        }

        return out;
    }

    public String toJsonString() {
        return toJson().toString();
    }

    @SuppressWarnings("unchecked")
    public static QueryRequest<?> fromJson(JsonObject json) {

        if (json == null) {
            throw new IllegalArgumentException("QueryRequest JSON cannot be null");
        }

        String factId = null;
        if(json.has("fact_id") && !json.get("fact_id").isJsonNull()){
            factId = json.get("fact_id").getAsString();
        }

        // ---- meta ----
        Meta meta = Meta.fromJson(json.getAsJsonObject("meta"));

        // ---- context ----
        GraphContext context = GraphContext.fromJson(json.getAsJsonObject("context"));

        // ---- task ----
        JsonObject taskJson = json.getAsJsonObject("task");
        String taskType = json.get("taskType").getAsString();

        QueryTask task;
        try {
            Class<?> taskClass = Class.forName(taskType);
            task = (QueryTask) taskClass
                    .getMethod("fromJson", JsonObject.class)
                    .invoke(null, taskJson);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to rehydrate QueryTask: " + taskType, e);
        }

        QueryRequest<?> req = new QueryRequest<>(meta, context,task, factId);

        // ---- rule (identity only) ----
        if (json.has("rule") && !json.get("rule").isJsonNull()) {
            // resolved later by pipeline wiring
            // leave as null here intentionally
        }

        // ---- adapter (identity only) ----
        if (json.has("adapter") && !json.get("adapter").isJsonNull()) {
            // resolved later by pipeline wiring
            // leave as null here intentionally
        }

        return req;
    }

}

