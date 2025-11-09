package ai.braineous.rag.prompt.models.cgo.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.graph.*;

import ai.braineous.rag.prompt.models.cgo.Fact;

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

    // -----------------------------------------------------------------------
}
