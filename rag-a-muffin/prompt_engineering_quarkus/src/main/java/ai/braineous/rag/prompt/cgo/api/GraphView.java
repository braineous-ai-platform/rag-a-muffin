package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.api.Fact;

public interface GraphView {
    Fact getFactById(String id);
}