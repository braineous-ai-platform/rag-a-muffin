package ai.braineous.cgo.llm;

import ai.braineous.rag.prompt.observe.Console;

import java.nio.charset.StandardCharsets;

public class LLMAdapterHttpPoster {

    public LLMAdapterHttpPoster() {
    }

    public HttpCallResult post(String endpoint, String jsonBody) throws Exception {

        String base = "http://127.0.0.1:8000";
        String url = base + "/" + endpoint;

        java.net.http.HttpClient client =
                java.net.http.HttpClient.newBuilder()
                        .version(java.net.http.HttpClient.Version.HTTP_1_1)
                        .build();

        Console.log("__________llm_adapter_url_______", url);
        Console.log("____payload____", jsonBody);

        java.net.http.HttpRequest request =
                java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                        .build();

        java.net.http.HttpResponse<String> resp =
                client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpCallResult result = new HttpCallResult(resp.statusCode(), resp.body());

        Console.log("____http_response____", result.toString());

        return result;
    }
}
