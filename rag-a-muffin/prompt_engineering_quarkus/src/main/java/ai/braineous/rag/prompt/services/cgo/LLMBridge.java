package ai.braineous.rag.prompt.services.cgo;

import java.util.List;

import ai.braineous.rag.prompt.models.cgo.Edge;

public interface LLMBridge {

    public void submit(List<FactExtractor> facts, Edge edge);
}
