package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.List;

import ai.braineous.rag.prompt.models.cgo.Fact;

public class LLMFacts {

    private String json;

    private List<Fact> facts;

    public LLMFacts(List<Fact> facts, String json) {
        this.facts = facts;
        this.json = json;
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
        StringBuilder sb = new StringBuilder();
        sb.append("LLMFacts{");
        sb.append("json=").append(json);
        sb.append(", facts=").append(facts);
        sb.append('}');
        return sb.toString();
    }
}
