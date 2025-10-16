package ai.braineous.rag.prompt.services.cgo;

import java.util.List;
import java.util.Map;

import ai.braineous.rag.prompt.models.cgo.Edge;
import ai.braineous.rag.prompt.models.cgo.ReasoningContext;
import ai.braineous.rag.prompt.models.cgo.ReasoningPath;

public interface ReasoningPathOrchestrator {
    ReasoningPath build(ReasoningContext cx, List<Edge> edges, Map<String,Object> cfg);
}
