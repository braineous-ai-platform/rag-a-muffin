package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;
import ai.braineous.rag.prompt.models.cgo.graph.SnapshotHash;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MutationOrchestratorTests {

    @Test
    public void testOrchestrateFlow() throws Exception{
        Console.log("mutation_event_orchestrator", "testOrchestrateFlow");

        Set<Proposal> proposals = new HashSet<>();
        MutationOrchestrator orch = MutationOrchestrator.getInstance();

        SnapshotHash s1 = new SnapshotHash("1");
        SnapshotHash s2 = new SnapshotHash("2");

        MutationResultListener l1 = orch.orchestrate(s1, proposals);
        MutationResultListener l2 = orch.orchestrate(s2, proposals);

        Console.log("l1", l1.result());
        Console.log("l2", l2.result());
    }

    @Test
    public void orchestrate_returnsListener_andProducesResultObject() {
        Set<Proposal> proposals = new HashSet<>();
        MutationOrchestrator orch = MutationOrchestrator.getInstance();

        SnapshotHash s1 = new SnapshotHash("1");
        MutationResultListener l1 = orch.orchestrate(s1, proposals);

        assertNotNull(l1, "listener must not be null");
        assertNotNull(l1.result(), "listener.result() must not be null after orchestrate");
    }

    @Test
    public void orchestrate_propagatesBaseSnapshotHash_intoMutationResult() {
        Set<Proposal> proposals = new HashSet<>();
        MutationOrchestrator orch = MutationOrchestrator.getInstance();

        SnapshotHash s1 = new SnapshotHash("1");
        MutationResultListener l1 = orch.orchestrate(s1, proposals);

        assertNotNull(l1.result());
        assertNotNull(l1.result().getSnapshotHash(), "result snapshot hash must not be null");
        assertEquals("1", l1.result().getSnapshotHash().getValue(), "base snapshot hash must match");
    }

    @Test
    public void orchestrate_resultAcceptedRejected_neverNull() {
        Set<Proposal> proposals = new HashSet<>();
        MutationOrchestrator orch = MutationOrchestrator.getInstance();

        SnapshotHash s1 = new SnapshotHash("1");
        MutationResultListener l1 = orch.orchestrate(s1, proposals);

        assertNotNull(l1.result());
        assertNotNull(l1.result().getAccepted(), "accepted must never be null");
        assertNotNull(l1.result().getRejected(), "rejected must never be null");
    }

    @Test
    public void orchestrate_twoCalls_produceIndependentResults() {
        Set<Proposal> proposals = new HashSet<>();
        MutationOrchestrator orch = MutationOrchestrator.getInstance();

        MutationResultListener l1 = orch.orchestrate(new SnapshotHash("1"), proposals);
        MutationResultListener l2 = orch.orchestrate(new SnapshotHash("2"), proposals);

        assertNotNull(l1.result());
        assertNotNull(l2.result());

        assertNotEquals(
                l1.result().getSnapshotHash().getValue(),
                l2.result().getSnapshotHash().getValue(),
                "each result must reflect the base snapshot hash for that call"
        );
    }

    @Test
    @org.junit.jupiter.api.Disabled("Enable after prioritize() is implemented")
    public void proposalOrdering_isDeterministic() {
        Set<Proposal> proposals = new HashSet<>();
        // create 3 proposals with ids/ruleIds that should sort deterministically
        // proposals.add(p2); proposals.add(p1); proposals.add(p3);

        MutationOrchestrator orch = MutationOrchestrator.getInstance();
        MutationResultListener l1 = orch.orchestrate(new SnapshotHash("1"), proposals);

        // assert accepted list order matches expected stable sort
        // assertEquals(List.of("P1","P2","P3"), l1.result().getAccepted().stream().map(Proposal::getId).toList());
    }
}
