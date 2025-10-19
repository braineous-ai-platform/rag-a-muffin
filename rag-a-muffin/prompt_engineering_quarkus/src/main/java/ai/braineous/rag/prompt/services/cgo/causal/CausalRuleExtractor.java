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

            rule.setId(id);

            rules.add(rule);
        }

        return rules;
    }

}
