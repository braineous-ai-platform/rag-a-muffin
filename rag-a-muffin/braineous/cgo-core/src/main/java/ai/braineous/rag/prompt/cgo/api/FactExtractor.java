package ai.braineous.rag.prompt.cgo.api;

import java.util.List;

@FunctionalInterface
public interface FactExtractor {

    public List<Fact> extract(String json);
}
