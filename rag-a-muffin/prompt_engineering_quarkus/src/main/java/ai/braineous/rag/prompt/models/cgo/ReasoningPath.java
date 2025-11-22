package ai.braineous.rag.prompt.models.cgo;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;

import java.util.ArrayList;
import java.util.List;

public class ReasoningPath {

    private List<Fact> nodes;

    private List<Edge> edges;

    private double confidence;

    private String closure;

    public ReasoningPath(){
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public ReasoningPath(List<Fact> nodes, List<Edge> edges, String closure, double confidence) {
        this.closure = closure;
        this.confidence = confidence;
        this.edges = edges;
        this.nodes = nodes;
    }

    public List<Fact> getNodes() {
        return nodes;
    }

    public void setNodes(List<Fact> nodes) {
        this.nodes = nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getClosure() {
        return closure;
    }

    public void setClosure(String closure) {
        this.closure = closure;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ReasoningPath{");
        sb.append("nodes=").append(nodes);
        sb.append(", edges=").append(edges);
        sb.append(", confidence=").append(confidence);
        sb.append(", closure=").append(closure);
        sb.append('}');
        return sb.toString();
    }

}
