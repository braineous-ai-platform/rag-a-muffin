package ai.braineous.rag.prompt.services.cgo;

import java.util.List;
import java.util.Map;

import ai.braineous.rag.prompt.models.cgo.Edge;
import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.models.cgo.ReasoningContext;
import ai.braineous.rag.prompt.models.cgo.Rule;

public interface RuleEngine {

    List<Rule> inferRules(List<Fact> facts, Map<String,Object> cfg);
  List<Edge> applyRules(ReasoningContext cx, List<Rule> rules, Map<String,Object> cfg);
}
