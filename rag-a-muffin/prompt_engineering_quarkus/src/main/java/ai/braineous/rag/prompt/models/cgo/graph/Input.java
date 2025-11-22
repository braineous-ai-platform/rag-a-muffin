package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;

public class Input {
    private Fact from;
    private Fact to;
    private Fact edge;

    public Input() {
    }

    public Input(Fact from, Fact to, Fact edge) {
        this.from = from;
        this.to = to;
        this.edge = edge;
    }

    public Fact getFrom() {
        return from;
    }

    public void setFrom(Fact from) {
        this.from = from;
    }

    public Fact getTo() {
        return to;
    }

    public void setTo(Fact to) {
        this.to = to;
    }

    public Fact getEdge() {
        return edge;
    }

    public void setEdge(Fact edge) {
        this.edge = edge;
    }

    @Override
    public String toString() {
        return "Input{" +
                "from=" + from +
                ", to=" + to +
                ", edge=" + edge +
                '}';
    }
}
