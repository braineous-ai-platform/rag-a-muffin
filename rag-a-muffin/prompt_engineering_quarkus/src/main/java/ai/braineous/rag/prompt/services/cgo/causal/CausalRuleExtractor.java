package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ai.braineous.rag.prompt.models.cgo.Rule;
import ai.braineous.rag.prompt.services.cgo.RuleExtractor;

public class CausalRuleExtractor implements RuleExtractor{

    @Override
    public List<Rule> extract(String prompt, JsonArray rulesArray) {
        List<Rule> rules = new ArrayList<>();

        for(int i=0; i<rulesArray.size(); i++){
            Rule rule = new Rule();

            JsonObject ruleJson = rulesArray.get(i).getAsJsonObject();

            String id = prompt + ":" + i;
            String type = ruleJson.get("type").getAsString();
            String transformer = ruleJson.get("transformer").getAsString();
            String instructions = ruleJson.get("instructions").getAsString();

            rule.setId(id);
            rule.setName(id);
            rule.setType(type);
            rule.setTransformer(transformer);
            rule.setInstructions(instructions);

            rules.add(rule);
        }

        return rules;
    }

}
