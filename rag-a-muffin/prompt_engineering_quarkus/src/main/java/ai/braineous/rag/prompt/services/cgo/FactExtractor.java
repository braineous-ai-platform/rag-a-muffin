package ai.braineous.rag.prompt.services.cgo;

import java.util.List;

import com.google.gson.JsonArray;

import ai.braineous.rag.prompt.models.cgo.Fact;

@FunctionalInterface
public interface FactExtractor {
    List<Fact> extract(String jsonArrayStr);
}
