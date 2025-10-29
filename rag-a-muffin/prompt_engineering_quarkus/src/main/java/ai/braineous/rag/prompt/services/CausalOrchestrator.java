package ai.braineous.rag.prompt.services;

import java.util.List;
import java.util.Map;

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

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private CausalFactExtractor factExtractor = new CausalFactExtractor();

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private RuleEngine ruleEngine = new CausalRuleEngine();

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private CausalSummarizer summarizer = new CausalSummarizer();
    
    public void orchestrate(String prompt, JsonArray queryEmbeddings, JsonObject query){
      JsonArray factsArray = new JsonArray();
      factsArray.add(query);
      factsArray.add(query);
      //JsonArray rulesArray = query.get("rules").getAsJsonArray();

      //generate_reasoning_context
      ReasoningContext reasoningContext = new ReasoningContext();
      List<Fact> facts = factExtractor.extract(prompt, factsArray);
      reasoningContext.setFacts(facts);

      //integrate_rule_components
      Map<String, Object> feats = reasoningContext.getFacts().get(0).getFeats();
      List<Rule> rules = this.ruleEngine.inferRules(facts, feats);
      List<Edge> edges = this.ruleEngine.applyRules(reasoningContext, rules, feats);

      //TODO: integrate_summarizer_components
      //TODO: start_here
    }

    public void orchestrate(LLMContext llmContext){
      List<Fact> allFacts = llmContext.getAllFacts();

      //generate_reasoning_context
      ReasoningContext reasoningContext = new ReasoningContext();
      reasoningContext.setFacts(allFacts);

      //TODO: integrate_rule_components and generate the subgraph
      //Map<String, Object> feats = reasoningContext.getFacts().get(0).getFeats();

      //TODO: rules inferred_at_app_level (?)
      //List<Rule> rules = this.ruleEngine.inferRules(facts, feats);
      //List<Edge> edges = this.ruleEngine.applyRules(reasoningContext, rules, feats);

      //TODO: integrate_summarizer_components

      Console.log("llm_bridge_orchestrate", reasoningContext);
    }
}


/**
 * 
 * // ReasoningPathBuilder.java (concept sketch)
// Goal: facts -> rules -> reasoning_path -> closure ("that's what it's all about")

package ai.cog.reasoning;

import java.util.*;
import java.util.stream.*;

// ---------- Domain Models ----------
record Fact(String id, String text, Map<String,Object> feats) {}

record Rule(String id,
            String name,
            double weight,
            // lhs fact ids or semantic patterns, rhs derivation
            List<String> lhsFactIds,
            java.util.function.Function<Context, Fact> derive) {}

record Edge(String ruleId, String fromFactId, String toFactId, double score) {}

record ReasoningPath(List<Fact> nodes, List<Edge> edges, double confidence, String closure) {}

record Context(List<Fact> facts, Map<String,Object> state) {
  Fact factById(String id) { return facts.stream().filter(f -> f.id().equals(id)).findFirst().orElse(null); }
}

// ---------- Interfaces ----------
interface FactExtractor {
  List<Fact> extract(String prompt, Map<String,Object> cfg);
}

interface RuleEngine {
  List<Rule> inferRules(List<Fact> facts, Map<String,Object> cfg);
  List<Edge> applyRules(Context cx, List<Rule> rules, Map<String,Object> cfg);
}

interface PathOrchestrator {
  ReasoningPath build(Context cx, List<Edge> edges, Map<String,Object> cfg);
}

interface Summarizer {
  String summarize(ReasoningPath path, Map<String,Object> cfg);
  String closurePhrase(); // e.g., "that's what it's all about"
}

// ---------- Default Implementations (sketch) ----------
class NlpFactExtractor implements FactExtractor {
  public List<Fact> extract(String prompt, Map<String,Object> cfg) {
    // Toy: split by parentheses or commas -> facts; tag with type hints if present
    List<Fact> out = new ArrayList<>();
    int i = 0;
    for (String s : prompt.split("\\)\\s*->\\s*|\\(|\\)|,")) {
      String t = s.trim();
      if (t.isEmpty()) continue;
      out.add(new Fact("F"+(i++), t, Map.of("source","prompt")));
    }
    return out;
  }
}

class HeuristicRuleEngine implements RuleEngine {
  public List<Rule> inferRules(List<Fact> facts, Map<String,Object> cfg) {
    // Example heuristics: performance ⇒ outcome; prep ⇒ confidence; friends ⇒ joy
    List<Rule> rules = new ArrayList<>();

    Rule performanceToOutcome = new Rule(
      "R1","performance->outcome",0.9,
      lhsIdsByContains(facts, "performance","effort","prep"),
      (cx) -> new Fact("F_outcome","win / trophy / positive outcome", Map.of("derivedBy","R1"))
    );

    Rule socialToJoy = new Rule(
      "R2","social->joy",0.7,
      lhsIdsByContains(facts, "friends","team","evening","fun"),
      (cx) -> new Fact("F_joy","fun evening with friends", Map.of("derivedBy","R2"))
    );

    Rule synthesis = new Rule(
      "R3","synthesis->day_quality",1.0,
      lhsIdsByContains(facts, "game","win","trophy","evening","fun","performance","prep"),
      (cx) -> new Fact("F_day","A great accomplished day.", Map.of("derivedBy","R3"))
    );

    rules.add(performanceToOutcome);
    rules.add(socialToJoy);
    rules.add(synthesis);
    return rules;
  }

  private List<String> lhsIdsByContains(List<Fact> facts, String... needles) {
    Set<String> nset = Arrays.stream(needles).collect(Collectors.toSet());
    return facts.stream()
      .filter(f -> nset.stream().anyMatch(k -> f.text().toLowerCase().contains(k)))
      .map(Fact::id)
      .collect(Collectors.toList());
  }

  public List<Edge> applyRules(Context cx, List<Rule> rules, Map<String,Object> cfg) {
    List<Edge> edges = new ArrayList<>();
    for (Rule r : rules) {
      if (r.lhsFactIds().isEmpty()) continue;
      Fact newFact = r.derive().apply(cx);
      if (newFact != null) {
        // attach derived fact into context state for orchestrator visibility
        @SuppressWarnings("unchecked")
        List<Fact> derivedFacts = (List<Fact>)cx.state().computeIfAbsent("derivedFacts", k -> new ArrayList<Fact>());
        derivedFacts.add(newFact);
        for (String lhs : r.lhsFactIds()) {
          edges.add(new Edge(r.id(), lhs, newFact.id(), r.weight()));
        }
      }
    }
    return edges;
  }
}

class GreedyPathOrchestrator implements PathOrchestrator {
  public ReasoningPath build(Context cx, List<Edge> edges, Map<String,Object> cfg) {
    // Build path: start from earliest prompt facts, walk highest-scoring edges to derived conclusions
    @SuppressWarnings("unchecked")
    List<Fact> derivedFacts = (List<Fact>)cx.state().getOrDefault("derivedFacts", List.of());
    Map<String,Fact> all = new HashMap<>();
    cx.facts().forEach(f -> all.put(f.id(), f));
    derivedFacts.forEach(f -> all.put(f.id(), f));

    // pick terminal nodes (only incoming edges)
    Set<String> hasOut = edges.stream().map(e -> e.fromFactId()).collect(Collectors.toSet());
    Set<String> hasIn  = edges.stream().map(e -> e.toFactId()).collect(Collectors.toSet());
    List<Fact> terminals = hasIn.stream().filter(id -> !hasOut.contains(id)).map(all::get).filter(Objects::nonNull).toList();

    // assemble a simple linearized path by sorting edges by score then sequence
    List<Edge> sorted = edges.stream().sorted((a,b)->Double.compare(b.score(), a.score())).toList();
    LinkedHashSet<String> order = new LinkedHashSet<>();
    for (Edge e : sorted) { order.add(e.fromFactId()); order.add(e.toFactId()); }
    List<Fact> pathFacts = order.stream().map(all::get).filter(Objects::nonNull).toList();

    double confidence = sorted.stream().mapToDouble(Edge::score).average().orElse(0.0);
    String closure = "that's what it's all about"; // closure phrase signature

    return new ReasoningPath(pathFacts, sorted, confidence, closure);
  }
}

class MinimalSummarizer implements Summarizer {
  public String summarize(ReasoningPath path, Map<String,Object> cfg) {
    // Prefer an explicit terminal “day” summary if present, else compress
    Optional<Fact> day = path.nodes().stream().filter(f -> f.text().toLowerCase().contains("great accomplished day")).findFirst();
    if (day.isPresent()) return day.get().text();
    String joined = path.nodes().stream().map(Fact::text).collect(Collectors.joining(" → "));
    return joined + " — " + closurePhrase();
  }
  public String closurePhrase() { return "that's what it's all about"; }
}

// ---------- Orchestrated Facade ----------
public class ReasoningPathBuilder {

  private final FactExtractor factExtractor = new NlpFactExtractor();
  private final RuleEngine ruleEngine = new HeuristicRuleEngine();
  private final PathOrchestrator orchestrator = new GreedyPathOrchestrator();
  private final Summarizer summarizer = new MinimalSummarizer();

  public Output run(String prompt, Map<String,Object> cfg) {
    boolean unitTest = Boolean.TRUE.equals(cfg.getOrDefault("unit_test_mode", false));
    Map<String,Object> state = new HashMap<>(Map.of("seed", cfg.getOrDefault("seed", 42)));

    List<Fact> facts = factExtractor.extract(prompt, cfg);
    Context cx = new Context(facts, state);
    List<Rule> rules = ruleEngine.inferRules(facts, cfg);
    List<Edge> edges = ruleEngine.applyRules(cx, rules, cfg);
    ReasoningPath path = orchestrator.build(cx, edges, cfg);
    String summary = summarizer.summarize(path, cfg);

    if (unitTest) {
      Unit.assertContains(summary, "great accomplished day", "Summary should conclude with the day-quality statement");
      Unit.assertEquals(path.closure(), summarizer.closurePhrase(), "Closure phrase must match signature");
      Unit.assertTrue(path.confidence() >= 0.6, "Confidence must meet minimum threshold");
    }
    return new Output(facts, rules, edges, path, summary);
  }

  // ---------- DTOs ----------
  public record Output(List<Fact> facts, List<Rule> rules, List<Edge> edges, ReasoningPath path, String summary) {}

  // ---------- Tiny Unit Helper ----------
  static class Unit {
    static void assertTrue(boolean cond, String msg){ if(!cond) throw new AssertionError(msg); }
    static void assertContains(String hay, String needle, String msg){ if (hay==null || !hay.toLowerCase().contains(needle)) throw new AssertionError(msg + " [needle="+needle+", got="+hay+"]"); }
    static void assertEquals(Object a, Object b, String msg){ if (!Objects.equals(a,b)) throw new AssertionError(msg + " [a="+a+", b="+b+"]"); }
  }

  // ---------- Example Usage ----------
  public static void main(String[] args) {
    String prompt = "(that excellent cricket game) -> (amazing performance/effort/prep) -> (win, trophy, fun evening of street cricket with friends)";
    Map<String,Object> cfg = Map.of(
      "unit_test_mode", true,
      "seed", 7
    );
    Output out = new ReasoningPathBuilder().run(prompt, cfg);
    System.out.println("Summary: " + out.summary());
    System.out.println("Closure: " + out.path.closure());
    System.out.println("Confidence: " + out.path.confidence());
  }
}

 * 
 */