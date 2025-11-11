package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.List;
import java.util.Set;

import ai.braineous.rag.prompt.models.cgo.Fact;

public class LLMFacts {

    private String json;

    private List<Fact> facts;

    public LLMFacts() {
    }

    public LLMFacts(String json, List<Fact> facts) {
        this.json = json;
        this.facts = facts;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    @Override
    public String toString() {
        return "LLMFacts [json=" + json + ", facts=" + facts + "]";
    }
}
