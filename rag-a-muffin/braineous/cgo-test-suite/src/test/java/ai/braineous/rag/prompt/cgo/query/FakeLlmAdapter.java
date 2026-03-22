package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.LlmAdapter;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;

public class FakeLlmAdapter extends LlmAdapter {

    private String response;

    public FakeLlmAdapter() {
        this("{\"result\":{\"ok\":true}}");
    }

    public FakeLlmAdapter(String response) {
        this.response = response;
    }

    public FakeLlmAdapter(JsonObject config, String response) {
        super(config);
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public String invokeLlm(QueryRequest queryRequest, JsonObject prompt) {
        Console.log("_____fake_llm_adapter_query_request_____", queryRequest);
        Console.log("_____fake_llm_adapter_prompt_____", prompt);
        Console.log("_____fake_llm_adapter_response_____", response);
        return response;
    }
}
