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
    public String invokeLlm(QueryRequest queryRequest, JsonObject prompt){
        try {
            String response = null;

            //invoke LLM - stub for now
            //response = "{\"result\":{\"status\":\"VALID\"}}";

            response = """
                    {
                      "result": {
                        "ok": true,
                        "code": "response.contract.ok",
                        "message": "VALID",
                        "stage": "llm_response_validation",
                        "anchorId": null,
                        "metadata": { "adapter": "fake" }
                      }
                    }
                    """;

            //TODO: call llm-adapter, just wiring test
            //print result as Console.log. but keep
            //contract same for now, to aboid
            //updtream regressions at the top
            String endpoint = "invoke";
            HttpCallResult result = this.poster.post(endpoint, prompt.toString());
            Console.log("llm_adapter_response", result);

            return response;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
