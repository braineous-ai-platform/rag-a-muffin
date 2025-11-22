package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public class Proposal {

    // ------------ Identity / provenance ------------

    private String id;             // UUID for this proposal
    private String ruleId;         // which rule generated it (if any)
    private String rulepackId;     // which rulepack / pipeline
    private ProposalSource source; // RULE / LLM / USER / SYSTEM

    // ------------ Explainability / lifecycle ------------

    private String reason;         // human-readable why
    private Instant createdAt;     // when it was generated
    private Instant evaluatedAt;   // when MEHUL touched it
    private ProposalStatus status; // PENDING / APPROVED / REJECTED / PARTIAL

    // free-form tags: tenant, domain, severity, anything dashboardy
    private Map<String, String> tags;

    // ------------ The actual delta ------------

    private Set<Fact> insert;     // new facts to introduce
    private Set<Fact> update;     // existing facts with updated payload
    private Set<Fact> delete;     // facts to retire/remove
    private Set<Relationship> edges;  // edge operations (ADD/UPDATE/DELETE)

    // getters, setters, builders...


    public Proposal() {
    }

    public Proposal(String id) {
        this.id = id;
    }

    public Set<Fact> getInsert() {
        return insert;
    }

    public void setInsert(Set<Fact> insert) {
        this.insert = insert;
    }

    public Set<Fact> getUpdate() {
        return update;
    }

    public void setUpdate(Set<Fact> update) {
        this.update = update;
    }

    public Set<Fact> getDelete() {
        return delete;
    }

    public void setDelete(Set<Fact> delete) {
        this.delete = delete;
    }

    public Set<Relationship> getEdges() {
        return edges;
    }

    public void setEdges(Set<Relationship> edges) {
        this.edges = edges;
    }
}
