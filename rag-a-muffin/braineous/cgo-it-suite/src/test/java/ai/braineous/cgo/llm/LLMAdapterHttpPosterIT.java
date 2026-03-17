package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LLMAdapterHttpPosterIT {

    @Test
    void poster_calls_fastapi_invoke_endpoint() throws Exception {

        LLMAdapterHttpPoster poster = new LLMAdapterHttpPoster();

        String payload = "{\"prompt\":{}}";
        Console.log("payload", payload);

        HttpCallResult result = poster.post("invoke", payload);
        Console.log("http_response", result);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getStatusCode() >= 200);
        Assertions.assertTrue(result.getStatusCode() < 300);
    }
}