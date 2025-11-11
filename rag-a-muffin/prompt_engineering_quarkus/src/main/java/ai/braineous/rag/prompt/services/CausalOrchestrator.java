package ai.braineous.rag.prompt.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ai.braineous.rag.prompt.models.cgo.Edge;
import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.models.cgo.ReasoningContext;
import ai.braineous.rag.prompt.models.cgo.Rule;
import ai.braineous.rag.prompt.services.cgo.LLMContext;
import ai.braineous.rag.prompt.services.cgo.RuleEngine;
import ai.braineous.rag.prompt.services.cgo.causal.CausalFactExtractor;
import ai.braineous.rag.prompt.services.cgo.causal.CausalRuleEngine;
import ai.braineous.rag.prompt.services.cgo.causal.CausalSummarizer;
import ai.braineous.rag.prompt.utils.Console;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class CausalOrchestrator {

  // TODO: [eventually] : make_it_quarkus_containarized.
  // For now no dependency_injection overhead
  // @Inject
  private RuleEngine ruleEngine = new CausalRuleEngine();

  // TODO: [eventually] : make_it_quarkus_containarized.
  // For now no dependency_injection overhead
  // @Inject
  private CausalSummarizer summarizer = new CausalSummarizer();

  public void orchestrate(LLMContext llmContext) {
    List<Fact> allFacts = llmContext.getAllFacts();

    CausalRuleEngine ruleEngine = new CausalRuleEngine();
    ruleEngine.infer(allFacts);

    // generate_reasoning_context
    // ReasoningContext reasoningContext = new ReasoningContext();
    // reasoningContext.setFacts(allFacts);

    // TODO: integrate_rule_components and generate the subgraph
    // Map<String, Object> feats = reasoningContext.getFacts().get(0).getFeats();

    // TODO: rules inferred_at_app_level (?)
    // Map<String, Object> feats = new HashMap<>();
    // List<Rule> rules = this.ruleEngine.inferRules(allFacts, feats);
    // List<Edge> edges = this.ruleEngine.applyRules(reasoningContext, rules,
    // feats);

    // TODO: integrate_summarizer_components

    // Console.log("llm_bridge_orchestrate", reasoningContext);
    // Console.log("llm_bridge_orchestrate", rules);
  }
}