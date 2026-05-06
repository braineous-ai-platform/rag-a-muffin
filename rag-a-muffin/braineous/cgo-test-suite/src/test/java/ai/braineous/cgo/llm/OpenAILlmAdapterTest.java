package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class OpenAILlmAdapterTest {

    @Test
    public void test_1() {
        FakePoster poster =
                new FakePoster(
                        new HttpCallResult(
                                200,
                                "{\"rawResponse\":{\"response\":\"{\\\"result\\\":{\\\"decision\\\":\\\"CAPTURE\\\",\\\"reason\\\":\\\"RISK_LEVEL_LOW\\\",\\\"code\\\":\\\"00\\\"}}\"}}"
                        )
                );

        OpenAILlmAdapter adapter =
                new OpenAILlmAdapter(poster);

        String response =
                adapter.invokeLlm(buildRequest(), buildPrompt());

        Console.log("openai.adapter.response", response);
        Console.log("openai.adapter.endpoint", poster.endpoint);
        Console.log("openai.adapter.body", poster.body);

        Assertions.assertEquals(
                "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":\"RISK_LEVEL_LOW\",\"code\":\"00\"}}",
                response
        );

        Assertions.assertEquals("invoke", poster.endpoint);
        Assertions.assertNotNull(poster.body);

        JsonObject body =
                JsonParser.parseString(poster.body).getAsJsonObject();

        Assertions.assertTrue(body.has("requestId"));
        Assertions.assertEquals("decision", body.get("queryKind").getAsString());
        Assertions.assertTrue(body.has("llmQuery"));
        Assertions.assertEquals(
                "llama3",
                body.getAsJsonObject("llmQuery").get("model").getAsString()
        );
    }

    @Test
    public void test_2() {
        FakePoster poster =
                new FakePoster(null);

        OpenAILlmAdapter adapter =
                new OpenAILlmAdapter(poster);

        RuntimeException exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> adapter.invokeLlm(buildRequest(), buildPrompt())
                );

        Console.log("openai.adapter.null.result", exception.getMessage());

        Assertions.assertTrue(
                exception.getCause().getMessage().contains("llm-adapter returned null HttpCallResult")
        );
    }

    @Test
    public void test_3() {
        FakePoster poster =
                new FakePoster(
                        new HttpCallResult(
                                500,
                                "{\"error\":\"boom\"}"
                        )
                );

        OpenAILlmAdapter adapter =
                new OpenAILlmAdapter(poster);

        RuntimeException exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> adapter.invokeLlm(buildRequest(), buildPrompt())
                );

        Console.log("openai.adapter.bad.status", exception.getMessage());

        Assertions.assertTrue(
                exception.getCause().getMessage().contains("llm-adapter call failed with status: 500")
        );
    }

    @Test
    public void test_4() {
        FakePoster poster =
                new FakePoster(
                        new HttpCallResult(
                                200,
                                null
                        )
                );

        OpenAILlmAdapter adapter =
                new OpenAILlmAdapter(poster);

        RuntimeException exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> adapter.invokeLlm(buildRequest(), buildPrompt())
                );

        Console.log("openai.adapter.null.body", exception.getMessage());

        Assertions.assertTrue(
                exception.getCause().getMessage().contains("llm-adapter response body is null")
        );
    }

    @Test
    public void test_5() {
        FakePoster poster =
                new FakePoster(
                        new HttpCallResult(
                                200,
                                "{}"
                        )
                );

        OpenAILlmAdapter adapter =
                new OpenAILlmAdapter(poster);

        RuntimeException exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> adapter.invokeLlm(buildRequest(), buildPrompt())
                );

        Console.log("openai.adapter.missing.rawResponse", exception.getMessage());

        Assertions.assertTrue(
                exception.getCause().getMessage().contains("llm-adapter response missing rawResponse")
        );
    }

    @Test
    public void test_6() {
        FakePoster poster =
                new FakePoster(
                        new HttpCallResult(
                                200,
                                "{\"rawResponse\":{}}"
                        )
                );

        OpenAILlmAdapter adapter =
                new OpenAILlmAdapter(poster);

        RuntimeException exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> adapter.invokeLlm(buildRequest(), buildPrompt())
                );

        Console.log("openai.adapter.missing.response", exception.getMessage());

        Assertions.assertTrue(
                exception.getCause().getMessage().contains("llm-adapter rawResponse missing response")
        );
    }

    private QueryRequest<ValidateTask> buildRequest() {
        Meta meta =
                new Meta(
                        "v1",
                        "decision",
                        "pay decision"
                );

        GraphContext context =
                new GraphContext();

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        "PaymentRequest:PAY-1001",
                        Arrays.asList("decision", "reason", "code"),
                        Arrays.asList("CustomerAccount:CUST-2001")
                );

        return new QueryRequest<ValidateTask>(
                meta,
                context,
                task
        );
    }

    private JsonObject buildPrompt() {
        JsonObject prompt =
                new JsonObject();

        prompt.addProperty("model", "llama3");
        prompt.addProperty("prompt", "Return only JSON.");
        prompt.addProperty("stream", false);

        return prompt;
    }

    private static class FakePoster extends LLMAdapterHttpPoster {

        private final HttpCallResult result;

        private String endpoint;
        private String body;

        private FakePoster(HttpCallResult result) {
            this.result = result;
        }

        @Override
        public HttpCallResult post(String endpoint, String jsonBody) {
            this.endpoint = endpoint;
            this.body = jsonBody;

            return this.result;
        }
    }
}