package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.braineous.rag.prompt.models.cgo.Edge;
import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.models.cgo.ReasoningContext;
import ai.braineous.rag.prompt.models.cgo.Rule;
import ai.braineous.rag.prompt.services.cgo.RuleEngine;

public class CausalRuleEngine implements RuleEngine{

    @Override
    public List<Rule> inferRules(List<Fact> facts, Map<String, Object> feats) {
        //Console.log("causal_rule_engine_inferRules", this.getClass().getName());
        //Console.log("facts", facts);
        //Console.log("feats", feats);

        return new ArrayList<>();
    }

    @Override
    public List<Edge> applyRules(ReasoningContext ctx, List<Rule> rules, Map<String, Object> feats) {
        //Console.log("causal_rule_engine_applyRules", this.getClass().getName());
        //Console.log("reasoning_context", ctx);
        //Console.log("rules", rules);
        //Console.log("feats", feats);

        return new ArrayList<>();
    }

}
