package ai.braineous.rag.prompt.cgo.api;

import com.google.gson.JsonObject;

public interface GraphView {
    Fact getFactById(String id);
}