package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.models.cgo.graph.Rulepack;
import ai.braineous.rag.prompt.services.cgo.causal.LLMFacts;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLMContext {
    private final Map<String, LLMFacts> context = new HashMap<>();

    public LLMContext() {
    }

    public void build(String type, String jsonArrayStr,
            FactExtractor factExtractor,
                      RelationshipProvider relationshipProvider,
                      List<BusinessRule> businessRules) {
        this.validate(jsonArrayStr);
        try {
            if(factExtractor == null){
                return;
            }

            List<Fact> facts = factExtractor.extract(jsonArrayStr);


            List<Relationship> relationships = new ArrayList<>();
            if(relationshipProvider != null){
                relationships = relationshipProvider.provideRelationships(facts);
            }

            LLMFacts llmFacts = new LLMFacts(jsonArrayStr, facts,
                    relationships,
                    List.of(),
                    List.of(),
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
            List<Fact> cour = llmFacts.getFacts();
            if(cour != null) {
                facts.addAll(cour);
            }
        }

        return facts;
    }

    public List<Relationship> getAllRelationships() {
        List<Relationship> relationships = new ArrayList<>();

        for (var entry : this.context.entrySet()) {
            LLMFacts llmFacts = entry.getValue();
            List<Relationship> cour = llmFacts.getRelationships();
            if(cour != null) {
                relationships.addAll(cour);
            }
        }

        return relationships;
    }

    public Rulepack getRulepack(){
        Rulepack rulepack = new Rulepack();

        List<BusinessRule> rules = new ArrayList<>();
        for (var entry : this.context.entrySet()) {
            LLMFacts llmFacts = entry.getValue();
            List<BusinessRule> cour = llmFacts.getBusinessRules();
            if(cour != null) {
                rules.addAll(cour);
            }
        }

        rulepack.setRules(rules);

        return rulepack;
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
