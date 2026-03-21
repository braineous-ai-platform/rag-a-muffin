package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LLMAdapterHttpPosterIT {

    @Test
    void poster_posts_exact_literal_json_to_invoke() throws Exception {
        String jsonBody = """
            {
              "requestId": "1",
              "queryKind": "reroute_passengers",
              "llmQuery": {
                "model": "llama3",
                "prompt": "Why is the sky blue?",
                "stream": false
              }
            }
            """;

        Console.log("IT:LLMAdapterHttpPoster.literal_payload", jsonBody);

        LLMAdapterHttpPoster poster = new LLMAdapterHttpPoster();
        HttpCallResult result = poster.post("invoke", jsonBody);

        Console.log("IT:LLMAdapterHttpPoster.literal_result", result.toString());

        Assertions.assertNotNull(result);

        int status = result.getStatusCode();
        Assertions.assertTrue(status >= 200 && status < 300);

        String body = result.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertFalse(body.trim().isEmpty());
    }
}