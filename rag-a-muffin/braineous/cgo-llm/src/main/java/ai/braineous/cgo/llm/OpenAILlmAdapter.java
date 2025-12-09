package ai.braineous.cgo.llm;

import com.google.gson.JsonObject;

public class OpenAILlmAdapter implements LlmAdapter{

    @Override
    public String invokeLlm(JsonObject prompt){
        String response = null;

        //invoke LLM - stub for now
        response = "{\"result\":{\"status\":\"VALID\"}}";

        return response;
    }
}
