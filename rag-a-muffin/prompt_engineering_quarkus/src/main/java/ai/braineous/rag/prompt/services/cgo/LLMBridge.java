package ai.braineous.rag.prompt.services.cgo;

import java.util.List;
import java.util.Set;

import ai.braineous.rag.prompt.models.cgo.Fact;

public interface LLMBridge {

    public void submit(List<Fact> facts, Set<String> rules);
}
