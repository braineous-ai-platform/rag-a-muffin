package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LLMGraph {
    final MutableNetwork<Fact, RuleEdge> network = NetworkBuilder.directed()
            .allowsParallelEdges(true).build();

    final Map<String, Map<String, Object>> nodeAttrs = new HashMap<>();

    final Map<String, Set<String>> edgeAttrs = new HashMap<>();

    public LLMGraph() {
    }

    // ---insert_operations----------------------------------------------------
    public void addFact(Fact fact) {
        this.network.addNode(fact);
    }

    public void addEdge(Fact nodeU, Fact nodeV, RuleEdge ruleEdge) {
        this.network.addEdge(nodeU, nodeV, ruleEdge);

        this.nodeAttrs.put(nodeU.getId(), nodeU.getFeats());
        this.nodeAttrs.put(nodeV.getId(), nodeV.getFeats());

        // merge instead of overwrite
        this.edgeAttrs.merge(
                ruleEdge.getRule().getId(),
                new HashSet<>(ruleEdge.getEdge().getAttributes()),
                (oldSet, newSet) -> {
                    oldSet.addAll(newSet);
                    return oldSet;
                });
    }

    public BindResult bind(Input input){
        //Validate
        Validator validator = new Validator();
        BindResult bindResult = validator.bind(input);

        //update the Graph as in "addEdge" now private/internal

        //perform VersionControl

        return bindResult;
    }

    // -----------------------------------------------------------------------
}
