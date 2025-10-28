package ai.braineous.rag.prompt.services.cgo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.causal.LLMFacts;

public class LLMContext {
    private final Map<String, LLMFacts> context = new HashMap<>();

    public LLMContext() {
    }

    public void addFact(String type, String jsonArrayStr, Function<String, List<Fact>> factExtractor){
        //validate_proper json. Has to be a json_array
        JsonElement jsonElement = JsonParser.parseString(jsonArrayStr);
        if(!jsonElement.isJsonArray()){
            throw new RuntimeException("ivalid_inout_format: " + jsonArrayStr + " must be a valid JSON Array");
        }

        List<Fact> facts = factExtractor.apply(jsonArrayStr);

        LLMFacts llmFacts = new LLMFacts(facts, jsonArrayStr);

        context.put(type, llmFacts);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LLMContext{");
        sb.append("context=").append(context);
        sb.append('}');
        return sb.toString();
    }
}
