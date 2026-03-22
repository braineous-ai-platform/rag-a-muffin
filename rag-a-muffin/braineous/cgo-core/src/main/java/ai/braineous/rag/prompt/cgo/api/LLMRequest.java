package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import com.google.gson.JsonObject;

public class LLMRequest extends CGOBaseModel{

    private QueryRequest queryRequest;

    private JsonObject llmQuery;

    //--------------------------------------------
    public LLMRequest() {
    }

    //-------------------------------------------

    public QueryRequest getQueryRequest() {
        return queryRequest;
    }

    public void setQueryRequest(QueryRequest queryRequest) {
        this.queryRequest = queryRequest;
    }

    public JsonObject getLlmQuery() {
        return llmQuery;
    }

    public void setLlmQuery(JsonObject llmQuery) {
        this.llmQuery = llmQuery;
    }
}
