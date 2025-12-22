package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.cgo.api.LlmAdapter;
import com.google.gson.JsonObject;

public class OpenAILlmAdapter extends LlmAdapter {

    public OpenAILlmAdapter() {
        super();
    }

    public OpenAILlmAdapter(JsonObject config) {
        super(config);
    }

    @Override
    public String invokeLlm(JsonObject prompt){
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

        return response;
    }
}
