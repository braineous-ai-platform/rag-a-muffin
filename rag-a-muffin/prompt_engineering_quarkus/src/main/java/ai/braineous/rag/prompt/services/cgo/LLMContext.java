package ai.braineous.rag.prompt.services.cgo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.causal.LLMFacts;
import ai.braineous.rag.prompt.utils.Console;

public class LLMContext {
    private final Map<String, LLMFacts> context = new HashMap<>();

    public LLMContext() {
    }

    public void addFact(String type, String jsonArrayStr, Function<String, List<Fact>> factExtractor){
        //TODO: validate_proper json. Has to be a json_array

        List<Fact> facts = factExtractor.apply(jsonArrayStr);
        Console.log("extracted_facts", facts);

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
