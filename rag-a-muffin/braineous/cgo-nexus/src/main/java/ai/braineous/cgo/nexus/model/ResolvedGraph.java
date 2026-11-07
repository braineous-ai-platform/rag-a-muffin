package ai.braineous.cgo.nexus.model;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;

public class ResolvedGraph {

    private Fact axis;
    private GraphSnapshot snapshot;

    public ResolvedGraph(
            Fact axis,
            GraphSnapshot snapshot) {
        this.axis = axis;
        this.snapshot = snapshot;
    }

    public Fact getAxis() {
        return axis;
    }

    public void setAxis(Fact axis) {
        this.axis = axis;
    }

    public GraphSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(GraphSnapshot snapshot) {
        this.snapshot = snapshot;
    }
}
