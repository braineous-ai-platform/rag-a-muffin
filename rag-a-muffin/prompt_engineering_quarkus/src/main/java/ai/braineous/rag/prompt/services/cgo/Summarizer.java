package ai.braineous.rag.prompt.services.cgo;

import java.util.Map;

import ai.braineous.rag.prompt.models.cgo.ReasoningPath;

public interface Summarizer {
    String summarize(ReasoningPath path, Map<String,Object> cfg);
  String closurePhrase(); // e.g., "that's what it's all about"
}
