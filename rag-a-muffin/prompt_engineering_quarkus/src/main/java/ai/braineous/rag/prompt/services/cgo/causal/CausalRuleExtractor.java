package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;

import ai.braineous.rag.prompt.models.cgo.Rule;
import ai.braineous.rag.prompt.services.cgo.RuleExtractor;

public class CausalRuleExtractor implements RuleExtractor{

    @Override
    public List<Rule> extract(String prompt, JsonArray rulesArray) {
        List<Rule> rules = new ArrayList<>();


        return rules;
    }

}
