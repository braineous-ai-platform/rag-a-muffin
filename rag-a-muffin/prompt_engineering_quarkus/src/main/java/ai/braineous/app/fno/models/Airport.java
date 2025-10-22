package ai.braineous.app.fno.models;

import java.util.List;

import com.google.gson.JsonArray;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.FactExtractor;

public class Airport implements FactExtractor{

    @Override
    public List<Fact> extract(String prompt, JsonArray facts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
