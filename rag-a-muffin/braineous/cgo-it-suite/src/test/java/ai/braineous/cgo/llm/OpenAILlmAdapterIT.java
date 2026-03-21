package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OpenAILlmAdapterIT {

    @Test
    void invokeLlm_returns_body_on_2xx_response() {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setMeta(new Meta("v1", "reroute_passengers", "test"));

        String expectedRequestId = queryRequest.generateRequestId();

        JsonObject prompt = new JsonObject();
        prompt.addProperty("model", "llama3");
        prompt.addProperty("prompt", "Why is the sky blue?");
        prompt.addProperty("stream", false);

        OpenAILlmAdapter adapter = new OpenAILlmAdapter();
        String response = adapter.invokeLlm(queryRequest, prompt);

        JsonObject root = JsonParser.parseString(response).getAsJsonObject();

        Assertions.assertTrue(root.has("llmRequest"));
        Assertions.assertTrue(root.has("rawResponse"));
        Assertions.assertTrue(root.has("success"));

        JsonObject llmRequest = root.getAsJsonObject("llmRequest");

        Assertions.assertEquals(expectedRequestId, llmRequest.get("requestId").getAsString());
        Assertions.assertEquals("reroute_passengers", llmRequest.get("queryKind").getAsString());
        Assertions.assertTrue(root.get("success").getAsBoolean());
        Assertions.assertFalse(root.get("rawResponse").isJsonNull());
    }
}
