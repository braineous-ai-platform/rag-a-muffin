package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.FactValidatorRule;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import ai.braineous.rag.prompt.cgo.api.RelationshipValidatorRule;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProposalValidatorTests {

    @BeforeEach
    void setup() {
        GraphBuilder.getInstance().clear();
    }

    @Test
    void validate_returns_true_when_no_proposals_present() {
        ProposalValidator validator = ProposalValidator.getInstance();

        ProposalContext ctx = new ProposalContext();
        ctx.setProposals(new HashSet<>());
        ctx.setSnapshot(GraphBuilder.getInstance().snapshot());

        boolean result = validator.validate(ctx);

        assertTrue(result);
    }

    @Test
    void validate_accepts_valid_insert_fact() {
        ProposalValidator validator = ProposalValidator.getInstance();

        Fact airport = new Fact("Airport:AUS", "AUS");
        airport.setMode("atomic");

        Proposal proposal = new Proposal();
        proposal.setInsert(Set.of(airport));
        proposal.setUpdate(Set.of());
        proposal.setDelete(Set.of());
        proposal.setEdges(Set.of());

        ProposalContext ctx = new ProposalContext();
        ctx.setProposals(Set.of(proposal));
        ctx.setSnapshot(GraphBuilder.getInstance().snapshot());

        boolean result = validator.validate(ctx);

        assertTrue(result);
    }

    @Test
    void validate_rejects_invalid_insert_fact() {
        ProposalValidator validator = ProposalValidator.getInstance();

        Fact bad = new Fact("", "bad");
        bad.setMode("atomic");

        Proposal proposal = new Proposal();
        proposal.setInsert(Set.of(bad));
        proposal.setUpdate(Set.of());
        proposal.setDelete(Set.of());
        proposal.setEdges(Set.of());

        ProposalContext ctx = new ProposalContext();
        ctx.setProposals(Set.of(proposal));
        ctx.setSnapshot(GraphBuilder.getInstance().snapshot());

        boolean result = validator.validate(ctx);

        assertFalse(result);
    }

    @Test
    void validate_rejects_update_when_fact_not_in_snapshot() {
        ProposalValidator validator = ProposalValidator.getInstance();

        Fact missing = new Fact("Airport:DFW", "DFW");
        missing.setMode("atomic");

        Proposal proposal = new Proposal();
        proposal.setInsert(Set.of());
        proposal.setUpdate(Set.of(missing));
        proposal.setDelete(Set.of());
        proposal.setEdges(Set.of());

        ProposalContext ctx = new ProposalContext();
        ctx.setProposals(Set.of(proposal));
        ctx.setSnapshot(GraphBuilder.getInstance().snapshot());

        boolean result = validator.validate(ctx);

        assertFalse(result);
    }

    @Test
    void validate_accepts_valid_relationship_against_snapshot() {
        GraphBuilder builder = GraphBuilder.getInstance();

        Fact aus = new Fact("Airport:AUS", "AUS");
        aus.setMode("atomic");
        Fact dfw = new Fact("Airport:DFW", "DFW");
        dfw.setMode("atomic");

        builder.addNode(aus);
        builder.addNode(dfw);

        GraphSnapshot snapshot = builder.snapshot();

        Fact edgeFact = new Fact("Edge:AUS->DFW", "connects");
        edgeFact.setMode("relational");

        Relationship rel = new Relationship();
        rel.setFrom(aus);
        rel.setTo(dfw);
        rel.setEdge(edgeFact);

        Proposal proposal = new Proposal();
        proposal.setInsert(Set.of());
        proposal.setUpdate(Set.of());
        proposal.setDelete(Set.of());
        proposal.setEdges(Set.of(rel));

        ProposalContext ctx = new ProposalContext();
        ctx.setProposals(Set.of(proposal));
        ctx.setSnapshot(snapshot);

        boolean result = ProposalValidator.getInstance().validate(ctx);

        assertTrue(result);
    }

    @Test
    void validate_rejects_relationship_with_missing_node() {
        GraphBuilder builder = GraphBuilder.getInstance();

        Fact aus = new Fact("Airport:AUS", "AUS");
        aus.setMode("atomic");
        builder.addNode(aus);

        GraphSnapshot snapshot = builder.snapshot();

        Fact missing = new Fact("Airport:DFW", "DFW");
        missing.setMode("atomic");

        Fact edgeFact = new Fact("Edge:AUS->DFW", "connects");
        edgeFact.setMode("relational");

        Relationship rel = new Relationship();
        rel.setFrom(aus);
        rel.setTo(missing);
        rel.setEdge(edgeFact);

        Proposal proposal = new Proposal();
        proposal.setInsert(Set.of());
        proposal.setUpdate(Set.of());
        proposal.setDelete(Set.of());
        proposal.setEdges(Set.of(rel));

        ProposalContext ctx = new ProposalContext();
        ctx.setProposals(Set.of(proposal));
        ctx.setSnapshot(snapshot);

        boolean result = ProposalValidator.getInstance().validate(ctx);

        assertFalse(result);
    }
}

