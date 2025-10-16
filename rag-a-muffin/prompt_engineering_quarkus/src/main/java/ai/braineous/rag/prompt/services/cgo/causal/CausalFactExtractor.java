package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.List;
import java.util.Map;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.FactExtractor;

public class CausalFactExtractor implements FactExtractor{

    @Override
    public List<Fact> extract(String prompt, Map<String, Object> cfg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
