package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.List;
import java.util.Map;

import ai.braineous.rag.prompt.models.cgo.Edge;
import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.models.cgo.ReasoningContext;
import ai.braineous.rag.prompt.models.cgo.Rule;
import ai.braineous.rag.prompt.services.cgo.RuleEngine;

public class CausalRuleEngine implements RuleEngine{

    @Override
    public List<Rule> inferRules(List<Fact> facts, Map<String, Object> cfg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Edge> applyRules(ReasoningContext cx, List<Rule> rules, Map<String, Object> cfg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
