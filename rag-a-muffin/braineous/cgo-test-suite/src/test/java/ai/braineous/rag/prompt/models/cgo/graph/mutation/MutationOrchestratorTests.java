package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphStore;
import ai.braineous.rag.prompt.models.cgo.graph.GraphStoreImpl;
import ai.braineous.rag.prompt.models.cgo.graph.Proposal;
import ai.braineous.rag.prompt.models.cgo.graph.SnapshotHash;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MutationOrchestratorTests {

    @Test
    public void testOrchestrateFlow() throws Exception {
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
    void prioritize_is_idempotent_for_same_event_and_same_snapshot() {
        // Arrange
        ProposalOrderOrchestrator orch = ProposalOrderOrchestrator.getInstance();

        SnapshotHash h = new SnapshotHash();
        h.setValue(GraphStoreImpl.getInstance().snapshot().snapshotHash().getValue());

        MutationEvent event = new MutationEvent();
        event.setSnapshotHash(h);

        // Build proposals (order should NOT matter because Set)
        Proposal p1 = new Proposal();
        Proposal p2 = new Proposal();
        Proposal p3 = new Proposal();

        java.util.Set<Proposal> set = new java.util.HashSet<>();
        set.add(p2); set.add(p1); set.add(p3);
        event.setProposals(set);

        // Act (run multiple times)
        String a0 = pickIds(orch.prioritize(event).get("accepted"));
        String r0 = pickIds(orch.prioritize(event).get("rejected"));

        for(int i=0;i<25;i++){
            String ai = pickIds(orch.prioritize(event).get("accepted"));
            String ri = pickIds(orch.prioritize(event).get("rejected"));

            org.junit.jupiter.api.Assertions.assertEquals(a0, ai, "accepted changed on run " + i);
            org.junit.jupiter.api.Assertions.assertEquals(r0, ri, "rejected changed on run " + i);
        }
    }

    @Test
    void prioritize_rejects_all_when_event_snapshot_is_stale() {
        ProposalOrderOrchestrator orch = ProposalOrderOrchestrator.getInstance();

        // Intentionally stale snapshot hash
        SnapshotHash stale = new SnapshotHash();
        stale.setValue("stale-hash-value");

        MutationEvent event = new MutationEvent();
        event.setSnapshotHash(stale);

        Proposal p1 = new Proposal();
        Proposal p2 = new Proposal();

        java.util.Set<Proposal> set = new java.util.HashSet<>();
        set.add(p2);
        set.add(p1);
        event.setProposals(set);

        Map<String, java.util.List<Proposal>> out = orch.prioritize(event);

        // accepted must be empty
        org.junit.jupiter.api.Assertions.assertTrue(out.get("accepted").isEmpty());

        // rejected must contain exactly one
        org.junit.jupiter.api.Assertions.assertEquals(1, out.get("rejected").size());

        // deterministic: smallest generated id wins
        String expected =
                java.util.stream.Stream.of(p1, p2)
                        .map(Proposal::getId)
                        .sorted()
                        .findFirst()
                        .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                expected,
                out.get("rejected").get(0).getId()
        );
    }

    @Test
    void prioritize_is_idempotent_even_if_proposal_set_insertion_order_changes() {
        ProposalOrderOrchestrator orch = ProposalOrderOrchestrator.getInstance();

        // Match current snapshot so proposals can be accepted
        SnapshotHash h = GraphStoreImpl.getInstance().snapshot().snapshotHash();

        Proposal p1 = new Proposal();
        Proposal p2 = new Proposal();
        Proposal p3 = new Proposal();

        // Baseline run (one insertion order)
        MutationEvent e0 = new MutationEvent();
        e0.setSnapshotHash(h);

        java.util.Set<Proposal> s0 = new java.util.HashSet<>();
        s0.add(p2); s0.add(p1); s0.add(p3);
        e0.setProposals(s0);

        Map<String, java.util.List<Proposal>> out0 = orch.prioritize(e0);
        String a0 = firstId(out0.get("accepted"));
        String r0 = firstId(out0.get("rejected"));

        // Repeat with different insertion orders (simulating nondeterministic upstream)
        for(int i=0;i<30;i++){
            MutationEvent ei = new MutationEvent();
            ei.setSnapshotHash(h);

            java.util.Set<Proposal> si = new java.util.HashSet<>();
            if(i % 3 == 0){ si.add(p1); si.add(p2); si.add(p3); }
            else if(i % 3 == 1){ si.add(p3); si.add(p1); si.add(p2); }
            else { si.add(p2); si.add(p3); si.add(p1); }

            ei.setProposals(si);

            Map<String, java.util.List<Proposal>> outi = orch.prioritize(ei);

            org.junit.jupiter.api.Assertions.assertEquals(a0, firstId(outi.get("accepted")), "accepted changed run=" + i);
            org.junit.jupiter.api.Assertions.assertEquals(r0, firstId(outi.get("rejected")), "rejected changed run=" + i);
        }
    }

    @Test
    void prioritize_is_idempotent_for_same_event_instance_repeated_calls() {
        ProposalOrderOrchestrator orch = ProposalOrderOrchestrator.getInstance();

        SnapshotHash h = GraphStoreImpl.getInstance().snapshot().snapshotHash();

        MutationEvent event = new MutationEvent();
        event.setSnapshotHash(h);

        Proposal p1 = new Proposal();
        Proposal p2 = new Proposal();
        Proposal p3 = new Proposal();

        java.util.Set<Proposal> set = new java.util.HashSet<>();
        set.add(p2); set.add(p1); set.add(p3);
        event.setProposals(set);

        Map<String, java.util.List<Proposal>> out0 = orch.prioritize(event);
        String a0 = firstId(out0.get("accepted"));
        String r0 = firstId(out0.get("rejected"));

        for(int i=0;i<50;i++){
            Map<String, java.util.List<Proposal>> outi = orch.prioritize(event);
            org.junit.jupiter.api.Assertions.assertEquals(a0, firstId(outi.get("accepted")), "accepted changed run=" + i);
            org.junit.jupiter.api.Assertions.assertEquals(r0, firstId(outi.get("rejected")), "rejected changed run=" + i);
        }
    }

    @Test
    void snapshotHash_is_stable_when_store_state_does_not_change() {
        GraphStore store = GraphStoreImpl.getInstance();

        SnapshotHash h1 = store.snapshot().snapshotHash();
        SnapshotHash h2 = store.snapshot().snapshotHash();

        org.junit.jupiter.api.Assertions.assertNotNull(h1);
        org.junit.jupiter.api.Assertions.assertNotNull(h2);
        org.junit.jupiter.api.Assertions.assertNotNull(h1.getValue());
        org.junit.jupiter.api.Assertions.assertNotNull(h2.getValue());

        org.junit.jupiter.api.Assertions.assertEquals(
                h1.getValue().trim(),
                h2.getValue().trim(),
                "snapshotHash changed without any mutation"
        );
    }

    @Test
    void orchestrate_enqueues_event_and_returns_listener_even_before_apply_is_wired() {
        GraphStore store = GraphStoreImpl.getInstance();
        SnapshotHash h = store.snapshot().snapshotHash();

        Proposal p = new Proposal();
        ensureProposalCollections(p);

        // any minimal proposal payload is fine; it won't apply yet
        Fact f = new Fact("Test:NoApplyYet:" + System.nanoTime(), "{ \"kind\":\"Test\" }");
        f.setMode("atomic");
        p.getInsert().add(f);

        MutationResultListener listener =
                MutationOrchestrator.getInstance().orchestrate(h, java.util.Set.of(p));

        org.junit.jupiter.api.Assertions.assertNotNull(listener);
    }


    private String firstId(java.util.List<Proposal> ps){
        if(ps == null || ps.isEmpty()) return "";
        Proposal p = ps.get(0);
        return (p.getId() == null) ? "" : p.getId().trim();
    }



    private String pickIds(java.util.List<Proposal> ps){
        if(ps == null || ps.isEmpty()) return "";
        Proposal p = ps.get(0);
        return (p.getId() == null) ? "" : p.getId().trim();
    }

    private void applyOneMutationThatChangesGraph(GraphStore store){
        SnapshotHash h = store.snapshot().snapshotHash();

        Proposal p = new Proposal();
        ensureProposalCollections(p);

        Fact f = new Fact(
                "Test:Node:Mutation",
                "{ \"id\":\"Test:Node:Mutation\", \"kind\":\"Test\" }"
        );
        f.setMode("atomic");

        p.getInsert().add(f);

        MutationOrchestrator.getInstance().orchestrate(h, java.util.Set.of(p));
    }


    private String waitUntilSnapshotHashChanges(GraphStore store, String before, long timeoutMs){
        long deadline = System.currentTimeMillis() + timeoutMs;

        String b = (before == null) ? "" : before.trim();

        while(System.currentTimeMillis() < deadline){
            String cur = store.snapshot().snapshotHash().getValue();
            String c = (cur == null) ? "" : cur.trim();

            if(!c.equals(b)){
                return c;
            }

            try { Thread.sleep(10); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("interrupted while waiting for mutation", e);
            }
        }

        // Last check for nicer failure message
        String finalHash = store.snapshot().snapshotHash().getValue();
        throw new AssertionError(
                "snapshotHash did not change within " + timeoutMs + "ms" +
                        " (before=" + b + ", after=" + ((finalHash == null) ? "null" : finalHash.trim()) + ")"
        );
    }

    private void ensureProposalCollections(Proposal p){
        if(p.getInsert() == null) p.setInsert(new java.util.HashSet<>());
        if(p.getUpdate() == null) p.setUpdate(new java.util.HashSet<>());
        if(p.getDelete() == null) p.setDelete(new java.util.HashSet<>());
        if(p.getEdges()  == null) p.setEdges(new java.util.HashSet<>());
    }


}
