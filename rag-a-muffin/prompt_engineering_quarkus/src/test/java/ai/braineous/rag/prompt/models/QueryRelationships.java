package ai.braineous.rag.prompt.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;

public class QueryRelationships {

    private JsonArray rules;
    private JsonArray facts;
    private JsonObject content;

    //graph with facts as nodes and rules as edges (content+embeddings could be weights, but give it deeper though for now)
    //TODO: deeper_thought_required 
    ImmutableGraph<String> factsAdjancyGraph;

    public QueryRelationships(){

    }

    public JsonArray getRules() {
        return rules;
    }

    public JsonArray getFacts() {
        return facts;
    }

    public JsonObject getContent() {
        return content;
    }

    public ImmutableGraph<String> getFactsAdjancyGraph() {
        return factsAdjancyGraph;
    }

    public void setRules(JsonArray rules) {
        this.rules = rules;
    }

    public void setFacts(JsonArray facts) {
        this.facts = facts;
    }

    public void setContent(JsonObject content) {
        this.content = content;
    }

    public void setFactsAdjancyGraph(ImmutableGraph<String> factsAdjancyGraph) {
        this.factsAdjancyGraph = factsAdjancyGraph;
    }

    //----------------------graph_theory_algorithms---------------------------------------
    //TODO: deeper_thought_required  
}
