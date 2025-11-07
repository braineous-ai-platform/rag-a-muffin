package ai.braineous.rag.prompt.services.cgo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.causal.LLMFacts;

public class LLMContext {
    private final Map<String, LLMFacts> context = new HashMap<>();

    public LLMContext() {
    }

    public void build(String type, String jsonArrayStr, 
        Function<String, List<Fact>> factExtractor,
        Function<List<Fact>, Set<String>> ruleProducer
    ){
        this.validate(jsonArrayStr);
        try{
            List<Fact> facts = factExtractor.apply(jsonArrayStr);
            Set<String> rules = ruleProducer.apply(facts);

            LLMFacts llmFacts = new LLMFacts(jsonArrayStr, facts, rules);
            context.put(type, llmFacts);
        }catch(Exception e){
            throw new RuntimeException("unkown_error: " + e.getMessage());
        }
    }

    public List<Fact> getAllFacts(){
        List<Fact> facts = new ArrayList<>();

        for(var entry: this.context.entrySet()){
            LLMFacts llmFacts = entry.getValue();
            facts.addAll(llmFacts.getFacts());
        }

        return facts;
    }

    public Set<String> getAllRules(){
        Set<String> rules = new LinkedHashSet<>();

        for(var entry: this.context.entrySet()){
            LLMFacts llmFacts = entry.getValue();
            rules.addAll(llmFacts.getRules());
        }

        return rules;
    }

    private void validate(String jsonArrayStr){
        //validate_proper json. Has to be a json_array
        JsonElement jsonElement = JsonParser.parseString(jsonArrayStr);
        if(!jsonElement.isJsonArray()){
            throw new RuntimeException("invalid_input_format: " + jsonArrayStr + " must be a valid JSON Array");
        }
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
