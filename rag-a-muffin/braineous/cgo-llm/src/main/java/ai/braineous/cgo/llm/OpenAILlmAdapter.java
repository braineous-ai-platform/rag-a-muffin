package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.cgo.api.LlmAdapter;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;

public class OpenAILlmAdapter extends LlmAdapter {

    private final LLMAdapterHttpPoster poster;

    public OpenAILlmAdapter() {
        super();
        this.poster = new LLMAdapterHttpPoster();
    }

    public OpenAILlmAdapter(JsonObject config) {
        super(config);
        this.poster = new LLMAdapterHttpPoster();
    }

    @Override
    public String invokeLlm(QueryRequest queryRequest, JsonObject prompt) {
        try {
            JsonObject adapterRequest = new JsonObject();
            adapterRequest.addProperty("requestId", queryRequest.getRequestId());
            adapterRequest.addProperty("queryKind", queryRequest.getMeta().getQueryKind());

            // llm_query
            adapterRequest.add("llmQuery", prompt);

            Console.log("llm_adapter_request", adapterRequest);

            String endpoint = "invoke";
            HttpCallResult result = this.poster.post(endpoint, adapterRequest.toString());
            Console.log("llm_adapter_response", result);

            if (result == null) {
                throw new IllegalStateException("llm-adapter returned null HttpCallResult");
            }

            int status = result.getStatusCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("llm-adapter call failed with status: " + status);
            }

            return result.getBody();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
