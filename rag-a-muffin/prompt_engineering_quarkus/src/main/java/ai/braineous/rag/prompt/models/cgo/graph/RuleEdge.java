package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Edge;
import ai.braineous.rag.prompt.models.cgo.Rule;

public class RuleEdge {
    private Rule rule;

    private Edge edge;

    public RuleEdge() {
    }

    public RuleEdge(Rule rule, Edge edge) {
        this.rule = rule;
        this.edge = edge;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public String toString() {
        return "RuleEdge [rule=" + rule + ", edge=" + edge + "]";
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }
}
