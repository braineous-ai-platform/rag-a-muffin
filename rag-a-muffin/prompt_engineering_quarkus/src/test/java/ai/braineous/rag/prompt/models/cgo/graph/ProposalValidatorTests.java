package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.utils.Console;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProposalValidatorTests {
    @Test
    public void testValidateSingleSimpleProposalSuccess() {
        Console.log("testValidateSingleSimpleProposalSuccess", null);

        // -------- arrange --------
        // minimal fact that matches the GraphView stub in ProposalValidator (id = "F1")
        Fact f1 = new Fact("F1", "{\"id\":\"F1\"}");

        // no updates/deletes/edges for the first driver test
        Set<Fact> inserts = Collections.singleton(f1);
        Set<Fact> updates = Collections.emptySet();
        Set<Fact> deletes = Collections.emptySet();
        Set<Relationship> edges = Collections.emptySet();

        // however your Proposal is constructed – constructor or setters
        Proposal proposal = new Proposal();
        proposal.setInsert(inserts);
        proposal.setUpdate(updates);
        proposal.setDelete(deletes);
        proposal.setEdges(edges);

        // ProposalContext holding a single proposal
        ProposalContext ctx = new ProposalContext();
        ctx.setProposals(Collections.singleton(proposal)); // or via ctor: new ProposalContext(Set.of(proposal))

        ProposalValidator validator = new ProposalValidator();

        // -------- act --------
        boolean result = validator.validate(ctx);

        // -------- assert --------
        // For now the contract is: returns true for a simple valid proposal.
        // As you add real validation logic, this test keeps the basic “happy path” pinned.
        assertTrue(result);
    }

    @Test
    public void testValidateEmptyContextIsOk() {
        Console.log("testValidateEmptyContextIsOk", null);

        ProposalContext ctx = new ProposalContext();
        ctx.setProposals(Collections.emptySet());

        ProposalValidator validator = new ProposalValidator();

        boolean result = validator.validate(ctx);

        assertTrue(result);
    }

    @Test
    public void testValidateSingleSimpleProposalFailsOnFactRule() {
        Console.log("testValidateSingleSimpleProposalFailsOnFactRule", null);

        // -------- arrange --------
        // minimal fact that matches the GraphView stub (id = "F1")
        Fact f1 = new Fact("F1", "{\"id\":\"F1\"}");

        Set<Fact> inserts = Collections.singleton(f1);
        Set<Fact> updates = Collections.emptySet();
        Set<Fact> deletes = Collections.emptySet();
        Set<Relationship> edges = Collections.emptySet();

        Proposal proposal = new Proposal();
        proposal.setInsert(inserts);
        proposal.setUpdate(updates);
        proposal.setDelete(deletes);
        proposal.setEdges(edges);

        ProposalContext ctx = new ProposalContext();
        ctx.setProposals(Collections.singleton(proposal));

        // one FactValidatorRule that ALWAYS fails → should make the whole validation fail
        FactValidatorRule failingRule = (fact, view) -> false;

        ctx.setFactValidatorRules(Collections.singleton(failingRule));
        ctx.setRelationshipValidatorRules(Collections.emptySet());

        ProposalValidator validator = new ProposalValidator();

        // -------- act --------
        boolean result = validator.validate(ctx);

        // -------- assert --------
        assertFalse(result);
    }

    @Test
    public void testValidateSingleSimpleProposalFailsOnRelationshipRule() {
        Console.log("testValidateSingleSimpleProposalFailsOnRelationshipRule", null);

        // -------- arrange --------
        // Minimal facts for relationship
        Fact from = new Fact("F1", "{\"id\":\"F1\"}");
        Fact to   = new Fact("F2", "{\"id\":\"F2\"}");
        Fact edge = new Fact("E1", "{\"id\":\"E1\",\"mode\":\"relational\"}");

        Relationship r = new Relationship(from, to, edge);

        Set<Fact> inserts = Collections.emptySet();
        Set<Fact> updates = Collections.emptySet();
        Set<Fact> deletes = Collections.emptySet();
        Set<Relationship> edges = Collections.singleton(r);

        Proposal proposal = new Proposal();
        proposal.setInsert(inserts);
        proposal.setUpdate(updates);
        proposal.setDelete(deletes);
        proposal.setEdges(edges);

        ProposalContext ctx = new ProposalContext();
        ctx.setProposals(Collections.singleton(proposal));

        // failing relationship rule → proposal should fail
        RelationshipValidatorRule failingRule = (relationship, view) -> false;

        ctx.setFactValidatorRules(Collections.emptySet());
        ctx.setRelationshipValidatorRules(Collections.singleton(failingRule));

        ProposalValidator validator = new ProposalValidator();

        // -------- act --------
        boolean result = validator.validate(ctx);

        // -------- assert --------
        assertFalse(result);
    }

    @Test
    public void testValidateMultipleProposalsFailsIfAnyProposalFails() {
        Console.log("testValidateMultipleProposalsFailsIfAnyProposalFails", null);

        // -------- arrange --------
        // Proposal 1: should PASS
        Fact okFact = new Fact("OK", "{\"id\":\"OK\"}");
        Proposal okProposal = new Proposal();
        okProposal.setInsert(Collections.singleton(okFact));
        okProposal.setUpdate(Collections.emptySet());
        okProposal.setDelete(Collections.emptySet());
        okProposal.setEdges(Collections.emptySet());

        // Proposal 2: should FAIL
        Fact badFact = new Fact("BAD", "{\"id\":\"BAD\"}");
        Proposal badProposal = new Proposal();
        badProposal.setInsert(Collections.singleton(badFact));
        badProposal.setUpdate(Collections.emptySet());
        badProposal.setDelete(Collections.emptySet());
        badProposal.setEdges(Collections.emptySet());

        // Context holds BOTH proposals
        ProposalContext ctx = new ProposalContext();
        Set<Proposal> proposals = new HashSet<>();
        proposals.add(okProposal);
        proposals.add(badProposal);
        ctx.setProposals(proposals);

        // Single FactValidatorRule:
        // - returns true for "OK"
        // - returns false for "BAD"
        FactValidatorRule rule = (fact, view) -> {
            String id = fact.getId();
            if ("BAD".equals(id)) {
                return false;
            }
            return true;
        };

        ctx.setFactValidatorRules(Collections.singleton(rule));
        ctx.setRelationshipValidatorRules(Collections.emptySet());

        ProposalValidator validator = new ProposalValidator();

        // -------- act --------
        boolean result = validator.validate(ctx);

        // -------- assert --------
        // Even though one proposal passes, the other fails → overall result must be false.
        assertFalse(result);
    }
}
