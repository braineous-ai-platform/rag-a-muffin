package ai.braineous.rag.prompt.services;

import java.util.List;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.cgo.api.LLMContext;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.ProposalMonitor;
import ai.braineous.rag.prompt.models.cgo.graph.Validator;
import ai.braineous.rag.prompt.observe.Console;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class CausalOrchestrator {

  public CausalOrchestrator() {

  }

  public GraphView orchestrate(LLMContext llmContext) {
    Validator validator = new Validator();
    ProposalMonitor proposalMonitor = new ProposalMonitor();
    GraphBuilder graphBuilder = new GraphBuilder(validator, proposalMonitor);

    List<Fact> allFacts = llmContext.getAllFacts();


    for(Fact fact: allFacts){
      graphBuilder.addNode(fact);
    }

    //todo: relationship_binding


    GraphView view = graphBuilder.snapshot();

    return view;
  }
}