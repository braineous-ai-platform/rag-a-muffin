package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.Relationship;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Proposal {
    private static final java.util.concurrent.atomic.AtomicLong EVENT_SEQ =
            new java.util.concurrent.atomic.AtomicLong(0L);

    // ------------ Identity / provenance ------------

    private String id;             // UUID for this proposal
    private String ruleId;         // which rule generated it (if any)
    private String rulepackId;     // which rulepack / pipeline
    private ProposalSource source; // RULE / LLM / USER / SYSTEM
    private Rulepack rulepack;

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
        long seq = Proposal.nextSeq();
        this.id = Proposal.nextEventId(seq);
    }

    public Proposal(String id) {
        this.id = id;
    }

    public static Proposal from(Fact from, Fact to, Fact edge){
        if(from == null || to == null || edge == null){
            return null;
        }
        Proposal proposal = new Proposal();
        Set<Fact> inserts = new HashSet<>();
        Set<Fact> updates = new HashSet<>();
        Set<Fact> deletes = new HashSet<>();

        inserts.add(from);
        inserts.add(to);

        Set<Relationship> relationships = new HashSet<>();
        Relationship relationship = new Relationship(from, to, edge);
        relationships.add(relationship);

        proposal.setInsert(inserts);
        proposal.setUpdate(updates);
        proposal.setDelete(deletes);
        proposal.setEdges(relationships);

        return proposal;
    }

    public String getId() {
        return id;
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

    public Rulepack getRulepack() {
        return rulepack;
    }

    public void setRulepack(Rulepack rulepack) {
        this.rulepack = rulepack;
    }

    // call inside constructor
    private static long nextSeq() {
        return EVENT_SEQ.incrementAndGet();
    }

    // call inside constructor
    private static String nextEventId(long seq) {
        // readable + sortable-ish (by seq)
        return "ME-" + seq;
    }
}
