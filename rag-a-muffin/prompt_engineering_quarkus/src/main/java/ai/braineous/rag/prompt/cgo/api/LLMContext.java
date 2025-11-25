package ai.braineous.rag.prompt.cgo.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.FactExtractor;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.services.cgo.causal.LLMFacts;

public class LLMContext {
    private final Map<String, LLMFacts> context = new HashMap<>();

    public LLMContext() {
    }

    public void build(String type, String jsonArrayStr,
            FactExtractor factExtractor,
                      List<Relationship> relationships,
                      List<FactValidatorRule> factValidatorRules,
                      List<RelationshipValidatorRule> relationshipValidatorRules,
                      List<BusinessRule> businessRules) {
        this.validate(jsonArrayStr);
        try {
            List<Fact> facts = factExtractor.extract(jsonArrayStr);

            LLMFacts llmFacts = new LLMFacts(jsonArrayStr, facts,
                    relationships,
                    factValidatorRules,
                    relationshipValidatorRules,
                    businessRules
            );
            context.put(type, llmFacts);
        } catch (Exception e) {
            throw new RuntimeException("unkown_error: " + e.getMessage());
        }
    }

    public List<Fact> getAllFacts() {
        List<Fact> facts = new ArrayList<>();

        for (var entry : this.context.entrySet()) {
            LLMFacts llmFacts = entry.getValue();
            facts.addAll(llmFacts.getFacts());
        }

        return facts;
    }

    public List<Relationship> getAllRelationships() {
        List<Relationship> relationships = new ArrayList<>();

        for (var entry : this.context.entrySet()) {
            LLMFacts llmFacts = entry.getValue();
            relationships.addAll(llmFacts.getRelationships());
        }

        return relationships;
    }

    private void validate(String jsonArrayStr) {
        // validate_proper json. Has to be a json_array
        JsonElement jsonElement = JsonParser.parseString(jsonArrayStr);
        if (!jsonElement.isJsonArray()) {
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
