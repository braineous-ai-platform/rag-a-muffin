package ai.braineous.rag.prompt.models.cgo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Edge extends Fact {

    private String ruleId;

    private String fromFactId;

    private String toFactId;

    private double score;

    private Set<String> attributes;

    public Edge() {
        this.attributes = new HashSet<>();
    }

    public Edge(Set<String> attributes) {
        this.attributes = attributes;
    }

    public Edge(String ruleId, String fromFactId, String toFactId, double score, Set<String> attributes) {
        this.ruleId = ruleId;
        this.fromFactId = fromFactId;
        this.toFactId = toFactId;
        this.score = score;
        this.attributes = attributes;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getFromFactId() {
        return fromFactId;
    }

    public void setFromFactId(String fromFactId) {
        this.fromFactId = fromFactId;
    }

    public String getToFactId() {
        return toFactId;
    }

    public void setToFactId(String toFactId) {
        this.toFactId = toFactId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void addAttribute(String attribute) {
        this.attributes.add(attribute);
    }

    public void removeAttribute(String attribute) {
        this.attributes.remove(attribute);
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "Edge [ruleId=" + ruleId + ", fromFactId=" + fromFactId + ", toFactId=" + toFactId + ", score=" + score
                + ", attributes=" + attributes + "]";
    }
}
