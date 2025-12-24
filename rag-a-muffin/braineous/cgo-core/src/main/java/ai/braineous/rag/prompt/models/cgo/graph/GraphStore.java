package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;

import java.util.Map;

public interface GraphStore {

    public void clear();
    public void upsertNode(Fact fact);

    public Map<String, Fact> nodes();

    public Map<String, Edge> edges();

    public GraphSnapshot snapshot();

    public void mutate(Fact from, Fact to, Fact edgeFact);
}
