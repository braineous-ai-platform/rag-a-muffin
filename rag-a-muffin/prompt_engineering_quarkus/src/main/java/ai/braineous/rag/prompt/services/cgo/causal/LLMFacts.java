package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.List;
import java.util.Set;

import ai.braineous.rag.prompt.models.cgo.Fact;

public class LLMFacts {

    private String json;

    private List<Fact> facts;

    private Set<String> rules;

    public LLMFacts() {
    }

    
    public LLMFacts(String json, List<Fact> facts,Set<String> rules) {
        this.json = json;
        this.facts = facts;
        this.rules = rules;
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

    public Set<String> getRules() {
        return rules;
    }

    public void setRules(Set<String> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LLMFacts{");
        sb.append("json=").append(json);
        sb.append(", facts=").append(facts);
        sb.append(", rules=").append(rules);
        sb.append('}');
        return sb.toString();
    }

}
