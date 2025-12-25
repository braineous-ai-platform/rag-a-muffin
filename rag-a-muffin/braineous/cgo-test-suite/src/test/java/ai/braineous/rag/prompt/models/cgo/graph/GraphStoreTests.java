package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GraphStoreTests {

    /**
     * This test verifies GraphStore.upsertNode is idempotent:
     * upserting the SAME node (same id + same payload) twice results in:
     *  - first upsert: snapshot changes
     *  - second upsert: snapshot does NOT change (converged / no-op)
     *
     * Why this matters:
     * - Substrate must be stable under retries / duplicate deliveries
     * - Prevents snapshot churn and false "new state" signals
     */
    @Test
    void upsertNode_is_idempotent_second_upsert_same_fact_snapshot_unchanged() {
        Console.log("test.start", "GraphStore.upsertNode.idempotent_same_fact");

        GraphStore store = GraphStoreImpl.getInstance();

        SnapshotHash s0 = store.snapshot().snapshotHash();
        String h0 = (s0 == null) ? null : s0.getValue();
        Console.log("store.snapshotHash.before", String.valueOf(h0));

        String id = "Fact:GS:Upsert:" + System.nanoTime();
        Fact f = new Fact(id, "{ \"id\":\"" + id + "\", \"kind\":\"GS\", \"v\":1 }");
        f.setMode("atomic");
        Console.log("fact.id", id);

        // First upsert -> should change snapshot
        store.upsertNode(f);

        SnapshotHash s1 = store.snapshot().snapshotHash();
        String h1 = (s1 == null) ? null : s1.getValue();
        Console.log("store.snapshotHash.after1", String.valueOf(h1));

        assertNotNull(h0);
        assertNotNull(h1);
        assertNotEquals(h0, h1);

        // Second upsert of identical fact -> should NOT change snapshot
        store.upsertNode(f);

        SnapshotHash s2 = store.snapshot().snapshotHash();
        String h2 = (s2 == null) ? null : s2.getValue();
        Console.log("store.snapshotHash.after2", String.valueOf(h2));

        assertEquals(h1, h2);

        Console.log("test.pass", "GraphStore.upsertNode.idempotent_same_fact");
    }

    /**
     * This test verifies GraphStore.deleteNode is idempotent:
     * deleting the SAME node twice results in:
     *  - first delete: snapshot changes
     *  - second delete: snapshot does NOT change (already absent)
     *
     * Why this matters:
     * - Substrate must be stable under retries / duplicate delete deliveries
     * - Prevents snapshot churn and false "new state" signals
     */
    @Test
    void deleteNode_is_idempotent_second_delete_snapshot_unchanged() {
        Console.log("test.start", "GraphStore.deleteNode.idempotent");

        GraphStore store = GraphStoreImpl.getInstance();

        // Seed node so first delete has an effect
        String id = "Fact:GS:Delete:" + System.nanoTime();
        Fact f = new Fact(id, "{ \"id\":\"" + id + "\", \"kind\":\"GS\", \"v\":1 }");
        f.setMode("atomic");
        store.upsertNode(f);
        Console.log("seed.id", id);

        SnapshotHash s0 = store.snapshot().snapshotHash();
        String h0 = (s0 == null) ? null : s0.getValue();
        Console.log("store.snapshotHash.beforeDelete", String.valueOf(h0));

        // First delete -> should change snapshot
        store.deleteNode(f);

        SnapshotHash s1 = store.snapshot().snapshotHash();
        String h1 = (s1 == null) ? null : s1.getValue();
        Console.log("store.snapshotHash.afterDelete1", String.valueOf(h1));

        assertNotNull(h0);
        assertNotNull(h1);
        assertNotEquals(h0, h1);

        // Second delete -> should NOT change snapshot
        store.deleteNode(f);

        SnapshotHash s2 = store.snapshot().snapshotHash();
        String h2 = (s2 == null) ? null : s2.getValue();
        Console.log("store.snapshotHash.afterDelete2", String.valueOf(h2));

        assertEquals(h1, h2);

        Console.log("test.pass", "GraphStore.deleteNode.idempotent");
    }

    /**
     * This test verifies GraphStore.mutate is idempotent for edges:
     * mutating the SAME relationship twice results in:
     *  - first mutate: snapshot changes
     *  - second mutate: snapshot does NOT change (edge already present)
     *
     * Why this matters:
     * - Protects substrate from duplicate relationship deliveries
     * - Ensures snapshotHash stability under retries
     * - Keeps Commit/Mutation idempotency assumptions valid
     */
    @Test
    void mutateEdge_is_idempotent_second_mutate_snapshot_unchanged() {
        Console.log("test.start", "GraphStore.mutateEdge.idempotent");

        GraphStore store = GraphStoreImpl.getInstance();

        // Seed endpoints
        String fromId = "Fact:GS:From:" + System.nanoTime();
        String toId   = "Fact:GS:To:" + System.nanoTime();

        Fact from = new Fact(fromId, "{ \"id\":\"" + fromId + "\", \"kind\":\"GS\" }");
        from.setMode("atomic");
        Fact to = new Fact(toId, "{ \"id\":\"" + toId + "\", \"kind\":\"GS\" }");
        to.setMode("atomic");

        store.upsertNode(from);
        store.upsertNode(to);

        // Prepare relationship
        String edgeId = "Edge:GS:" + System.nanoTime();
        Fact edge = new Fact(edgeId, "{ \"id\":\"" + edgeId + "\", \"kind\":\"REL\" }");
        edge.setMode("relational");

        Console.log("from.id", fromId);
        Console.log("to.id", toId);
        Console.log("edge.id", edgeId);

        SnapshotHash s0 = store.snapshot().snapshotHash();
        String h0 = (s0 == null) ? null : s0.getValue();
        Console.log("store.snapshotHash.before", String.valueOf(h0));

        // First mutate -> should change snapshot
        store.mutate(from, to, edge);

        SnapshotHash s1 = store.snapshot().snapshotHash();
        String h1 = (s1 == null) ? null : s1.getValue();
        Console.log("store.snapshotHash.after1", String.valueOf(h1));

        assertNotNull(h0);
        assertNotNull(h1);
        assertNotEquals(h0, h1);

        // Second mutate of same relationship -> should NOT change snapshot
        store.mutate(from, to, edge);

        SnapshotHash s2 = store.snapshot().snapshotHash();
        String h2 = (s2 == null) ? null : s2.getValue();
        Console.log("store.snapshotHash.after2", String.valueOf(h2));

        assertEquals(h1, h2);

        Console.log("test.pass", "GraphStore.mutateEdge.idempotent");
    }

    /**
     * This test verifies SnapshotHash determinism:
     * inserting the same logical content in different orders yields the SAME snapshot hash.
     *
     * Setup:
     * - Clear assumption: test operates on a fresh store instance OR uses unique ids
     * - Create nodes A, B and edge E (A->B)
     * - Apply in Order #1: upsert A, upsert B, mutate E
     * - Capture hash H1
     * - Apply in Order #2 to a fresh store: upsert B, upsert A, mutate E
     * - Capture hash H2
     *
     * Expected:
     * - H1 == H2
     *
     * Why this matters:
     * - Ensures snapshotHash is stable and not dependent on insertion ordering
     * - Protects deterministic reasoning + observer comparisons
     *
     * IMPORTANT:
     * - This test requires GraphStore to be resettable between runs OR you run it
     *   against two independent store instances. If GraphStoreImpl is a singleton,
     *   expose a test-only reset hook (e.g., GraphStoreImpl.resetForTests()) and call it.
     */
    @Test
    void snapshotHash_is_deterministic_independent_of_insertion_order() {
        Console.log("test.start", "GraphStore.snapshotHash.deterministic_order_independent");

        // --- Order #1 (A then B) ---
        GraphStore store1 = GraphStoreImpl.getInstance();
        // GraphStoreImpl.resetForTests(); // <-- if available, call BEFORE using store1

        String aId = "Fact:GS:Det:A:" + System.nanoTime();
        String bId = "Fact:GS:Det:B:" + System.nanoTime();
        String eId = "Edge:GS:Det:E:" + System.nanoTime();

        Fact a = new Fact(aId, "{ \"id\":\"" + aId + "\", \"kind\":\"Det\" }");
        a.setMode("atomic");
        Fact b = new Fact(bId, "{ \"id\":\"" + bId + "\", \"kind\":\"Det\" }");
        b.setMode("atomic");
        Fact e = new Fact(eId, "{ \"id\":\"" + eId + "\", \"kind\":\"DetRel\" }");
        e.setMode("relational");

        store1.upsertNode(a);
        store1.upsertNode(b);
        store1.mutate(a, b, e);

        SnapshotHash h1s = store1.snapshot().snapshotHash();
        String h1 = (h1s == null) ? null : h1s.getValue();
        Console.log("hash.order1", String.valueOf(h1));

        assertNotNull(h1);

        // --- Order #2 (B then A) on a fresh store ---
        // If singleton, you MUST reset here, otherwise this is meaningless.
        // GraphStoreImpl.resetForTests(); // <-- required if singleton
        GraphStore store2 = GraphStoreImpl.getInstance();

        // Recreate same ids/content (same logical content)
        Fact a2 = new Fact(aId, "{ \"id\":\"" + aId + "\", \"kind\":\"Det\" }");
        a2.setMode("atomic");
        Fact b2 = new Fact(bId, "{ \"id\":\"" + bId + "\", \"kind\":\"Det\" }");
        b2.setMode("atomic");
        Fact e2 = new Fact(eId, "{ \"id\":\"" + eId + "\", \"kind\":\"DetRel\" }");
        e2.setMode("relational");

        store2.upsertNode(b2);
        store2.upsertNode(a2);
        store2.mutate(a2, b2, e2);

        SnapshotHash h2s = store2.snapshot().snapshotHash();
        String h2 = (h2s == null) ? null : h2s.getValue();
        Console.log("hash.order2", String.valueOf(h2));

        assertNotNull(h2);

        // Deterministic regardless of insertion order
        assertEquals(h1, h2);

        Console.log("test.pass", "GraphStore.snapshotHash.deterministic_order_independent");
    }


}
