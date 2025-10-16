package ai.braineous.rag.prompt.services.cgo;

import java.util.List;
import java.util.Map;

import ai.braineous.rag.prompt.models.cgo.Fact;

public interface FactExtractor {
    List<Fact> extract(String prompt, Map<String,Object> cfg);
}
