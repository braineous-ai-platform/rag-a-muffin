package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;

public interface GraphView {
    Fact getFactById(String id);
}