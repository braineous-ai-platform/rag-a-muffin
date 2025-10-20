package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.Map;

import ai.braineous.rag.prompt.models.cgo.ReasoningPath;
import ai.braineous.rag.prompt.services.cgo.Summarizer;

public class CausalSummarizer implements Summarizer{

    @Override
    public String summarize(ReasoningPath path, Map<String, Object> cfg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String closurePhrase() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
