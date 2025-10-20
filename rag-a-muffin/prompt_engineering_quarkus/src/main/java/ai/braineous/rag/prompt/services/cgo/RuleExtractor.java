package ai.braineous.rag.prompt.services.cgo;

import java.util.List;

import com.google.gson.JsonArray;

import ai.braineous.rag.prompt.models.cgo.Rule;

public interface RuleExtractor {
    List<Rule> extract(String prompt, JsonArray rules);
}
