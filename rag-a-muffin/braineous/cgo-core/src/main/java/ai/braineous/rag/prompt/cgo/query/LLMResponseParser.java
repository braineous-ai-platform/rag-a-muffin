package ai.braineous.rag.prompt.cgo.query;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LLMResponseParser {

    public LLMResponseParser() {
    }

    public JsonObject parse(String rawResponse) {
        if (rawResponse == null) {
            throw new IllegalArgumentException("rawResponse cannot be null");
        }
        if (rawResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("rawResponse cannot be blank");
        }
        return JsonParser.parseString(rawResponse).getAsJsonObject();
    }
}
