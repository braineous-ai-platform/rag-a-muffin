package ai.braineous.rag.prompt.services.cgo;

import java.util.List;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.models.cgo.Rule;

public interface LLMBridge {

    public void submit(List<Fact> facts, List<Rule> rules);
}
