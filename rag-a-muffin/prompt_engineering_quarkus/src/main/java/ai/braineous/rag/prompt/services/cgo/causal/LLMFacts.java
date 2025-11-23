package ai.braineous.rag.prompt.services.cgo.causal;

import ai.braineous.rag.prompt.cgo.api.*;

import java.util.List;

public class LLMFacts {

    private String json;

    private List<Fact> facts;

    private List<Relationship> relationships;

    private List<FactValidatorRule> factValidatorRules;

    private List<RelationshipValidatorRule> relationshipValidatorRules;

    private List<BusinessRule> businessRules;

    public LLMFacts() {
    }

    public LLMFacts(String json, List<Fact> facts) {
        this.json = json;
        this.facts = facts;
    }

    public LLMFacts(String json, List<Fact> facts, List<Relationship> relationships,
                    List<FactValidatorRule> factValidatorRules,
                    List<RelationshipValidatorRule> relationshipValidatorRules,
                    List<BusinessRule> businessRules) {
        this.json = json;
        this.facts = facts;
        this.relationships = relationships;
        this.factValidatorRules = factValidatorRules;
        this.relationshipValidatorRules = relationshipValidatorRules;
        this.businessRules = businessRules;
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

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }

    public List<FactValidatorRule> getFactValidatorRules() {
        return factValidatorRules;
    }

    public void setFactValidatorRules(List<FactValidatorRule> factValidatorRules) {
        this.factValidatorRules = factValidatorRules;
    }

    public List<RelationshipValidatorRule> getRelationshipValidatorRules() {
        return relationshipValidatorRules;
    }

    public void setRelationshipValidatorRules(List<RelationshipValidatorRule> relationshipValidatorRules) {
        this.relationshipValidatorRules = relationshipValidatorRules;
    }

    public List<BusinessRule> getBusinessRules() {
        return businessRules;
    }

    public void setBusinessRules(List<BusinessRule> businessRules) {
        this.businessRules = businessRules;
    }

    @Override
    public String toString() {
        return "LLMFacts{" +
                "json='" + json + '\'' +
                ", facts=" + facts +
                ", relationships=" + relationships +
                ", factValidatorRules=" + factValidatorRules +
                ", relationshipValidatorRules=" + relationshipValidatorRules +
                ", businessRules=" + businessRules +
                '}';
    }
}
