package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.cgo.api.LlmAdapter;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    OpenAILlmAdapter(LLMAdapterHttpPoster poster) {
        super();
        this.poster = poster;
    }

    @Override
    public String invokeLlm(QueryRequest queryRequest, JsonObject prompt) {
        try {
            JsonObject adapterRequest = new JsonObject();
            adapterRequest.addProperty("requestId", queryRequest.getRequestId());
            adapterRequest.addProperty("queryKind", queryRequest.getMeta().getQueryKind());
            adapterRequest.add("llmQuery", prompt);

            Console.log("llm_adapter_request", adapterRequest);

            HttpCallResult result = this.poster.post("invoke", adapterRequest.toString());

            Console.log("llm_adapter_response", result);

            if (result == null) {
                throw new IllegalStateException("llm-adapter returned null HttpCallResult");
            }

            int status = result.getStatusCode();

            if (status < 200 || status >= 300) {
                throw new IllegalStateException("llm-adapter call failed with status: " + status);
            }

            return extractModelResponse(result.getBody());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extractModelResponse(String responseBody) {
        if (responseBody == null) {
            throw new IllegalStateException("llm-adapter response body is null");
        }

        JsonObject root =
                JsonParser.parseString(responseBody).getAsJsonObject();

        if (!root.has("rawResponse")) {
            throw new IllegalStateException("llm-adapter response missing rawResponse");
        }

        JsonObject rawResponse =
                root.getAsJsonObject("rawResponse");

        if (!rawResponse.has("response")) {
            throw new IllegalStateException("llm-adapter rawResponse missing response");
        }

        return rawResponse.get("response").getAsString();
    }
}