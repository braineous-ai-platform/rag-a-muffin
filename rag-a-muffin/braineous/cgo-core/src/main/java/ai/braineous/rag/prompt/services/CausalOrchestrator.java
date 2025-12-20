package ai.braineous.rag.prompt.services;

import java.util.List;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.cgo.api.LLMContext;
import ai.braineous.rag.prompt.models.cgo.graph.*;
import ai.braineous.rag.prompt.observe.Console;

public class CausalOrchestrator {

  public CausalOrchestrator() {

  }

  public GraphView orchestrate(LLMContext llmContext) {
    GraphBuilder graphBuilder = GraphBuilder.getInstance();

    List<Fact> allFacts = llmContext.getAllFacts();


    for(Fact fact: allFacts){
      graphBuilder.addNode(fact);
    }

    //relationship_binding
    bindAllRelationships(graphBuilder, llmContext);


    GraphView view = graphBuilder.snapshot();

    return view;
  }

  private static void bindAllRelationships(GraphBuilder graphBuilder, LLMContext llmContext) {
    var relationships = llmContext.getAllRelationships();
    if (relationships == null || relationships.isEmpty()) {
      Console.log("cgo.relationships.none", null);
      return;
    }

    for (var rel : relationships) {
      if (rel == null || rel.getFrom() == null || rel.getTo() == null || rel.getEdge() == null) {
        Console.log("cgo.relationships.skip.null", rel);
        continue;
      }

      Input input = new Input(rel.getFrom(), rel.getTo(), rel.getEdge());

      // substrate-only for now (rulepack null)
      Rulepack rulepack = llmContext.getRulepack();
      BindResult r = graphBuilder.bind(input, rulepack);

      if (!r.isOk()) {
        // important: GraphBuilder fails if from/to nodes not present in nodes map
        Console.log("cgo.relationships.bind.fail", input.toString());
      }
    }
  }
}