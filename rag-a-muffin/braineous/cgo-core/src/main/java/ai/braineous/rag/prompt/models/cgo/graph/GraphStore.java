package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;

import java.util.Map;

public interface GraphStore {

    public void upsertNode(Fact fact);

    public GraphSnapshot snapshot();

    public void deleteNode(Fact fact);

    public void mutate(Fact from, Fact to, Fact edgeFact);
}
