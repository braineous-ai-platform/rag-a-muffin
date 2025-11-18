package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.utils.Console;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

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

    //@Test
    public void testValidateEmptyContextIsOk() {
        Console.log("testValidateEmptyContextIsOk", null);

        ProposalContext ctx = new ProposalContext();
        ctx.setProposals(Collections.emptySet());

        ProposalValidator validator = new ProposalValidator();

        boolean result = validator.validate(ctx);

        assertTrue(result);
    }
}
