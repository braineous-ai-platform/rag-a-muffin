package ai.braineous.cgo.nexus.model;

import ai.braineous.rag.prompt.cgo.api.Fact;
import java.util.List;
import java.util.Map;

public class ResolvedGraph {

    private Fact axis;
    private Map<String, List<Fact>> relatedFacts;

    public ResolvedGraph(
            Fact axis,
            Map<String, List<Fact>> relatedFacts) {
        this.axis = axis;
        this.relatedFacts = relatedFacts;
    }

    public Fact getAxis() {
        return axis;
    }

    public void setAxis(Fact axis) {
        this.axis = axis;
    }

    public Map<String, List<Fact>> getRelatedFacts() {
        return relatedFacts;
    }

    public void setRelatedFacts(Map<String, List<Fact>> relatedFacts) {
        this.relatedFacts = relatedFacts;
    }
}
