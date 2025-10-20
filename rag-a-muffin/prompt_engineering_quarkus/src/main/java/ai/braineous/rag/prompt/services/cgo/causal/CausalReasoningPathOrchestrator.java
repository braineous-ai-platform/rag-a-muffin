package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.List;
import java.util.Map;

import ai.braineous.rag.prompt.models.cgo.Edge;
import ai.braineous.rag.prompt.models.cgo.ReasoningContext;
import ai.braineous.rag.prompt.models.cgo.ReasoningPath;
import ai.braineous.rag.prompt.services.cgo.ReasoningPathOrchestrator;

public class CausalReasoningPathOrchestrator implements ReasoningPathOrchestrator{

    @Override
    public ReasoningPath build(ReasoningContext cx, List<Edge> edges, Map<String, Object> cfg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
