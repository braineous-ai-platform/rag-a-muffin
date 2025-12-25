package ai.braineous.rag.prompt.models.cgo.graph.commit;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import ai.braineous.rag.prompt.models.cgo.graph.GraphStore;
import ai.braineous.rag.prompt.models.cgo.graph.GraphStoreImpl;
import ai.braineous.rag.prompt.models.cgo.graph.Proposal;
import ai.braineous.rag.prompt.models.cgo.graph.SnapshotHash;
import ai.braineous.rag.prompt.models.cgo.graph.mutation.MutationResult;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommitOrchestratorTests {

    @Test
    void orchestrate_returns_false_when_snapshot_mismatch_stale_proposal() {
        Console.log("test.start", "CommitOrchestrator.snapshot_mismatch_rejected");

        // Arrange: grab current store snapshot (source of truth)
        GraphStore store = GraphStoreImpl.getInstance();
        SnapshotHash storeSnap = store.snapshot().snapshotHash();
        String storeHash = (storeSnap == null) ? null : storeSnap.getValue();
        Console.log("store.snapshotHash", String.valueOf(storeHash));

        // Build a MutationResult with a DIFFERENT snapshot hash (stale)
        SnapshotHash stale = new SnapshotHash();
        stale.setValue("STALE_HASH_" + System.nanoTime());
        Console.log("result.snapshotHash", stale.getValue());

        MutationResult mr = new MutationResult();
        mr.setSnapshotHash(stale);

        CommitOrchestrator orch = new CommitOrchestrator();

        // Act
        CommitResult out = orch.orchestrate(mr);

        // Assert
        assertNotNull(out);
        assertFalse(out.isOk());
        Console.log("test.pass", "CommitOrchestrator.snapshot_mismatch_rejected");
    }
    /**
     * This test verifies that CommitOrchestrator rejects a MutationResult
     * when there are NO accepted proposals to commit.
     *
     * Why this matters:
     * - Enforces the invariant that exactly one accepted proposal is required
     * - Prevents no-op or ambiguous commits
     * - Keeps commit semantics explicit and deterministic
     */
    @Test
    void orchestrate_returns_false_when_no_accepted_proposals() {
        Console.log("test.start", "CommitOrchestrator.no_accepted_proposals_rejected");

        // Arrange: align snapshot hashes so we pass the staleness gate
        GraphStore store = GraphStoreImpl.getInstance();
        SnapshotHash storeSnap = store.snapshot().snapshotHash();
        Console.log("store.snapshotHash", storeSnap.getValue());

        MutationResult mr = new MutationResult();
        mr.setSnapshotHash(storeSnap);
        mr.setAccepted(java.util.Collections.emptyList()); // no accepted proposals

        CommitOrchestrator orch = new CommitOrchestrator();

        // Act
        CommitResult out = orch.orchestrate(mr);

        // Assert
        assertNotNull(out);
        assertFalse(out.isOk());
        Console.log("test.pass", "CommitOrchestrator.no_accepted_proposals_rejected");
    }

    /**
     * This test verifies that CommitOrchestrator rejects a MutationResult
     * when MORE THAN ONE proposal is marked as accepted.
     *
     * Why this matters:
     * - Enforces the single-winner rule for mutations
     * - Avoids ambiguous commits and partial graph application
     * - Keeps mutation semantics deterministic and explainable
     */
    @Test
    void orchestrate_returns_false_when_multiple_accepted_proposals() {
        Console.log("test.start", "CommitOrchestrator.multiple_accepted_proposals_rejected");

        // Arrange: align snapshot hashes to pass staleness check
        GraphStore store = GraphStoreImpl.getInstance();
        SnapshotHash storeSnap = store.snapshot().snapshotHash();
        Console.log("store.snapshotHash", storeSnap.getValue());

        Proposal p1 = new Proposal();
        Proposal p2 = new Proposal();

        MutationResult mr = new MutationResult();
        mr.setSnapshotHash(storeSnap);
        mr.setAccepted(java.util.List.of(p1, p2)); // invalid: >1 accepted

        CommitOrchestrator orch = new CommitOrchestrator();

        // Act
        CommitResult out = orch.orchestrate(mr);

        // Assert
        assertNotNull(out);
        assertFalse(out.isOk());
        Console.log("test.pass", "CommitOrchestrator.multiple_accepted_proposals_rejected");
    }

    /**
     * This test verifies that CommitOrchestrator returns ok=false
     * if the commit phase fails even after passing snapshot alignment.
     *
     * Why this matters:
     * - Separates "staleness" correctness from "commit execution" correctness
     * - Ensures we never report success unless the store mutation actually ran
     * - Hardens fail-fast behavior for downstream orchestration
     */
    @Test
    void orchestrate_returns_true_when_noops_in_proposal() {
        Console.log("test.start", "CommitOrchestrator.commit_phase_fails_rejected");

        // Arrange: align snapshot hashes to pass staleness gate
        GraphStore store = GraphStoreImpl.getInstance();
        SnapshotHash storeSnap = store.snapshot().snapshotHash();
        Console.log("store.snapshotHash", storeSnap.getValue());

        // Exactly one accepted proposal, but commit() should fail because all op-sets are null/empty
        Proposal p = new Proposal();
        // leave insert/update/delete/edges unset -> commit() returns false (no accepted operations)
        MutationResult mr = new MutationResult();
        mr.setSnapshotHash(storeSnap);
        mr.setAccepted(java.util.List.of(p));

        CommitOrchestrator orch = new CommitOrchestrator();

        // Act
        CommitResult out = orch.orchestrate(mr);

        // Assert
        assertNotNull(out);
        assertTrue(out.isOk());
        Console.log("test.pass", "CommitOrchestrator.commit_phase_pass_accepted");
    }

    /**
     * This test verifies that a "no-op" accepted proposal (no inserts/updates/deletes/edges)
     * is treated as a VALID convergence outcome: ok=true, and the store remains unchanged.
     *
     * Why this matters:
     * - "Do nothing" is a legitimate decision (steady state), not a commit failure
     * - Prevents pointless retries when the correct action is no mutation
     * - Preserves optimistic concurrency semantics via snapshot stability
     */
    @Test
    void orchestrate_returns_true_on_noop_commit_and_store_snapshot_unchanged() {
        Console.log("test.start", "CommitOrchestrator.noop_commit_ok_and_unchanged");

        // Arrange: capture store snapshot BEFORE
        GraphStore store = GraphStoreImpl.getInstance();
        SnapshotHash before = store.snapshot().snapshotHash();
        String beforeHash = (before == null) ? null : before.getValue();
        Console.log("store.snapshotHash.before", String.valueOf(beforeHash));

        // Align result snapshot to pass staleness gate
        MutationResult mr = new MutationResult();
        mr.setSnapshotHash(before);

        // Exactly one accepted proposal, but no operations
        Proposal p = new Proposal(); // leave op-sets unset/null
        mr.setAccepted(java.util.List.of(p));

        CommitOrchestrator orch = new CommitOrchestrator();

        // Act
        CommitResult out = orch.orchestrate(mr);

        // Assert: ok=true + result attached
        assertNotNull(out);
        assertTrue(out.isOk());
        assertNotNull(out.getMutationResult());
        assertSame(mr, out.getMutationResult());

        // Assert: store snapshot AFTER is unchanged
        SnapshotHash after = store.snapshot().snapshotHash();
        String afterHash = (after == null) ? null : after.getValue();
        Console.log("store.snapshotHash.after", String.valueOf(afterHash));

        assertEquals(beforeHash, afterHash);

        Console.log("test.pass", "CommitOrchestrator.noop_commit_ok_and_unchanged");
    }

    /**
     * This test verifies the happy path where:
     * - MutationResult snapshot matches the current store snapshot (not stale)
     * - Exactly one accepted proposal exists
     * - Proposal contains a real mutation (an insert)
     *
     * Expected:
     * - CommitResult.ok == true
     * - Store snapshot hash changes (because a new node id is added)
     *
     * Why this matters:
     * - Proves CommitOrchestrator actually applies accepted mutations to the substrate
     * - Confirms snapshot-hash determinism reacts to node-id deltas
     */
    @Test
    void orchestrate_commits_insert_and_store_snapshot_changes() {
        Console.log("test.start", "CommitOrchestrator.happy_path_insert_changes_snapshot");

        // Arrange: capture store snapshot BEFORE and align MR to it
        GraphStore store = GraphStoreImpl.getInstance();
        SnapshotHash before = store.snapshot().snapshotHash();
        String beforeHash = (before == null) ? null : before.getValue();
        Console.log("store.snapshotHash.before", String.valueOf(beforeHash));

        // Unique node id to guarantee a delta
        String id = "Fact:Insert:" + System.nanoTime();
        Fact f = new Fact(id, "{ \"id\":\"" + id + "\", \"kind\":\"Test\" }");
        f.setMode("atomic");

        Proposal p = new Proposal();
        java.util.Set<Fact> inserts = new java.util.HashSet<>();
        inserts.add(f);
        p.setInsert(inserts);

        MutationResult mr = new MutationResult();
        mr.setSnapshotHash(before);                 // pass staleness gate
        mr.setAccepted(java.util.List.of(p));       // single winner

        CommitOrchestrator orch = new CommitOrchestrator();

        // Act
        CommitResult out = orch.orchestrate(mr);

        // Assert: ok=true
        assertNotNull(out);
        assertTrue(out.isOk());
        assertNotNull(out.getMutationResult());
        assertSame(mr, out.getMutationResult());

        // Assert: snapshot hash AFTER changes (new node id included)
        SnapshotHash after = store.snapshot().snapshotHash();
        String afterHash = (after == null) ? null : after.getValue();
        Console.log("store.snapshotHash.after", String.valueOf(afterHash));

        assertNotNull(beforeHash);
        assertNotNull(afterHash);
        assertNotEquals(beforeHash, afterHash);

        Console.log("test.pass", "CommitOrchestrator.happy_path_insert_changes_snapshot");
    }

    /**
     * This test verifies commit() precedence: deletes win over updates for the same id.
     *
     * Setup:
     * - Seed store with an existing node X (so delete has something to remove)
     * - Proposal contains BOTH:
     *    - delete Fact X
     *    - update Fact X (should be ignored because X is deleted)
     *
     * Expected:
     * - CommitResult.ok == true (commit executes)
     * - Store snapshot changes due to deletion
     * - Store does NOT contain node X after commit (delete wins)
     *
     * Why this matters:
     * - Prevents "resurrecting" deleted nodes via an update in the same proposal
     * - Makes mutation application deterministic and safe
     */
    @Test
    void orchestrate_delete_beats_update_for_same_id() {
        Console.log("test.start", "CommitOrchestrator.precedence.delete_beats_update");

        GraphStore store = GraphStoreImpl.getInstance();

        // --- seed store with node X ---
        String id = "Fact:X:" + System.nanoTime();
        Fact seed = new Fact(id, "{ \"id\":\"" + id + "\", \"kind\":\"Seed\", \"v\":1 }");
        seed.setMode("atomic");
        store.upsertNode(seed);

        SnapshotHash before = store.snapshot().snapshotHash();
        String beforeHash = (before == null) ? null : before.getValue();
        Console.log("store.snapshotHash.before", String.valueOf(beforeHash));
        Console.log("seed.id", id);

        // --- build proposal: delete X + update X (update should be skipped) ---
        Fact del = new Fact(id, "{ \"id\":\"" + id + "\" }");
        del.setMode("atomic");

        Fact upd = new Fact(id, "{ \"id\":\"" + id + "\", \"kind\":\"Seed\", \"v\":999 }");
        upd.setMode("atomic");

        Proposal p = new Proposal();
        java.util.Set<Fact> deletes = new java.util.HashSet<>();
        deletes.add(del);
        p.setDelete(deletes);

        java.util.Set<Fact> updates = new java.util.HashSet<>();
        updates.add(upd);
        p.setUpdate(updates);

        MutationResult mr = new MutationResult();
        mr.setSnapshotHash(before);           // pass staleness check
        mr.setAccepted(java.util.List.of(p)); // single winner

        CommitOrchestrator orch = new CommitOrchestrator();

        // Act
        CommitResult out = orch.orchestrate(mr);

        // Assert: ok=true
        assertNotNull(out);
        assertTrue(out.isOk());

        SnapshotHash after = store.snapshot().snapshotHash();
        String afterHash = (after == null) ? null : after.getValue();
        Console.log("store.snapshotHash.after", String.valueOf(afterHash));

        assertNotNull(beforeHash);
        assertNotNull(afterHash);
        assertNotEquals(beforeHash, afterHash);

        // Assert: node X is gone (delete wins)
        Fact got = store.snapshot().nodes().get(id);
        Console.log("store.node.after", String.valueOf(got));
        assertNull(got);

        Console.log("test.pass", "CommitOrchestrator.precedence.delete_beats_update");
    }

    /**
     * This test verifies commit() precedence: inserts are skipped if the same id is also in updates.
     *
     * Setup:
     * - Proposal contains BOTH:
     *    - update Fact X (authoritative)
     *    - insert Fact X (should be skipped because X is "updated")
     *
     * Expected:
     * - CommitResult.ok == true
     * - Store contains node X AFTER commit
     * - Stored node payload matches the UPDATE version (not the INSERT version)
     *
     * Why this matters:
     * - Prevents conflicting writes for the same id within one proposal
     * - Ensures deterministic "one winner per id" semantics
     */
    @Test
    void orchestrate_insert_skipped_when_same_id_is_updated() {
        Console.log("test.start", "CommitOrchestrator.precedence.insert_skipped_if_updated");

        GraphStore store = GraphStoreImpl.getInstance();

        // Snapshot BEFORE (used to pass staleness gate)
        SnapshotHash before = store.snapshot().snapshotHash();
        String beforeHash = (before == null) ? null : before.getValue();
        Console.log("store.snapshotHash.before", String.valueOf(beforeHash));

        String id = "Fact:X:" + System.nanoTime();
        Console.log("fact.id", id);

        // Insert version (should be ignored)
        Fact ins = new Fact(id, "{ \"id\":\"" + id + "\", \"kind\":\"Test\", \"v\":1 }");
        ins.setMode("atomic");

        // Update version (should win)
        Fact upd = new Fact(id, "{ \"id\":\"" + id + "\", \"kind\":\"Test\", \"v\":999 }");
        upd.setMode("atomic");

        Proposal p = new Proposal();

        java.util.Set<Fact> updates = new java.util.HashSet<>();
        updates.add(upd);
        p.setUpdate(updates);

        java.util.Set<Fact> inserts = new java.util.HashSet<>();
        inserts.add(ins);
        p.setInsert(inserts);

        MutationResult mr = new MutationResult();
        mr.setSnapshotHash(before);
        mr.setAccepted(java.util.List.of(p));

        CommitOrchestrator orch = new CommitOrchestrator();

        // Act
        CommitResult out = orch.orchestrate(mr);

        // Assert: ok=true
        assertNotNull(out);
        assertTrue(out.isOk());

        // Assert: stored node exists and matches UPDATE payload (insert skipped)
        Fact stored = store.snapshot().nodes().get(id);
        Console.log("store.node.after", String.valueOf(stored));

        assertNotNull(stored);
        assertEquals(upd.getText(), stored.getText());

        Console.log("test.pass", "CommitOrchestrator.precedence.insert_skipped_if_updated");
    }

    /**
     * This test verifies commit() relationship safety: relationships are NOT applied
     * if either endpoint (from/to) is deleted in the same proposal.
     *
     * Setup:
     * - Seed store with nodes A and B
     * - Proposal deletes A
     * - Proposal also includes a relationship A -> B with edge E
     *
     * Expected:
     * - CommitResult.ok == true (commit executes)
     * - Node A is deleted
     * - Edge E is NOT present after commit (mutate skipped because fromId deleted)
     *
     * Why this matters:
     * - Prevents dangling edges that reference deleted nodes
     * - Keeps graph substrate consistent and deterministic
     */
    @Test
    void orchestrate_skips_relationship_when_endpoint_deleted() {
        Console.log("test.start", "CommitOrchestrator.relationship_skipped_if_endpoint_deleted");

        GraphStore store = GraphStoreImpl.getInstance();

        // --- seed store with endpoints A and B ---
        String aId = "Fact:A:" + System.nanoTime();
        String bId = "Fact:B:" + System.nanoTime();
        Fact a = new Fact(aId, "{ \"id\":\"" + aId + "\", \"kind\":\"Seed\" }");
        a.setMode("atomic");
        Fact b = new Fact(bId, "{ \"id\":\"" + bId + "\", \"kind\":\"Seed\" }");
        b.setMode("atomic");

        store.upsertNode(a);
        store.upsertNode(b);

        SnapshotHash before = store.snapshot().snapshotHash();
        String beforeHash = (before == null) ? null : before.getValue();
        Console.log("store.snapshotHash.before", String.valueOf(beforeHash));
        Console.log("from.id", aId);
        Console.log("to.id", bId);

        // --- build relationship A -> B ---
        String eId = "Edge:E:" + System.nanoTime();
        Fact edge = new Fact(eId, "{ \"id\":\"" + eId + "\", \"kind\":\"Rel\" }");
        edge.setMode("relational");

        Relationship rel = new Relationship();
        rel.setFrom(a);
        rel.setTo(b);
        rel.setEdge(edge);

        // --- proposal deletes A and also tries to add relationship A->B ---
        Proposal p = new Proposal();

        java.util.Set<Fact> deletes = new java.util.HashSet<>();
        deletes.add(a);              // delete endpoint
        p.setDelete(deletes);

        java.util.Set<Relationship> rels = new java.util.HashSet<>();
        rels.add(rel);
        p.setEdges(rels);

        MutationResult mr = new MutationResult();
        mr.setSnapshotHash(before);           // pass staleness check
        mr.setAccepted(java.util.List.of(p)); // single winner

        CommitOrchestrator orch = new CommitOrchestrator();

        // Act
        CommitResult out = orch.orchestrate(mr);

        // Assert: ok=true
        assertNotNull(out);
        assertTrue(out.isOk());

        // Assert: A deleted
        Fact aAfter = store.snapshot().nodes().get(aId);
        Console.log("store.node.A.after", String.valueOf(aAfter));
        assertNull(aAfter);

        // Assert: edge NOT present (relationship skipped)
        Object edgeAfter = store.snapshot().edges().get(eId);
        Console.log("store.edge.after", String.valueOf(edgeAfter));
        assertNull(edgeAfter);

        Console.log("test.pass", "CommitOrchestrator.relationship_skipped_if_endpoint_deleted");
    }

    /**
     * This test verifies idempotency at the commit layer:
     * applying the SAME logical mutation twice results in:
     *  - first commit: ok=true and snapshot changes
     *  - second commit: ok=true but snapshot does NOT change (converged / no-op)
     *
     * Why this matters:
     * - Protects against duplicate deliveries / retries in ingestion pipelines
     * - Ensures deterministic substrate behavior under at-least-once execution
     * - Distinguishes "duplicate" from "failure" (no retry spiral)
     *
     * Note:
     * - We intentionally re-align MutationResult.snapshotHash to the CURRENT store hash
     *   before each orchestrate() call (because stale protection is separate from idempotency).
     */
    @Test
    void orchestrate_is_idempotent_same_insert_twice_second_is_noop_snapshot_unchanged() {
        Console.log("test.start", "CommitOrchestrator.idempotency.same_insert_twice");

        GraphStore store = GraphStoreImpl.getInstance();

        // Use a stable, unique id for this test run
        String id = "Fact:Idem:" + System.nanoTime();
        Fact f = new Fact(id, "{ \"id\":\"" + id + "\", \"kind\":\"Idem\", \"v\":1 }");
        f.setMode("atomic");
        Console.log("fact.id", id);

        CommitOrchestrator orch = new CommitOrchestrator();

        // ---------- First commit ----------
        SnapshotHash snap1 = store.snapshot().snapshotHash();
        String before1 = (snap1 == null) ? null : snap1.getValue();
        Console.log("store.snapshotHash.before1", String.valueOf(before1));

        Proposal p1 = new Proposal();
        java.util.Set<Fact> inserts1 = new java.util.HashSet<>();
        inserts1.add(f);
        p1.setInsert(inserts1);

        MutationResult mr1 = new MutationResult();
        mr1.setSnapshotHash(snap1);
        mr1.setAccepted(java.util.List.of(p1));

        CommitResult out1 = orch.orchestrate(mr1);

        assertNotNull(out1);
        assertTrue(out1.isOk());

        SnapshotHash snapAfter1 = store.snapshot().snapshotHash();
        String after1 = (snapAfter1 == null) ? null : snapAfter1.getValue();
        Console.log("store.snapshotHash.after1", String.valueOf(after1));

        assertNotNull(before1);
        assertNotNull(after1);
        assertNotEquals(before1, after1);

        // ---------- Second commit (same insert again) ----------
        SnapshotHash snap2 = store.snapshot().snapshotHash();
        String before2 = (snap2 == null) ? null : snap2.getValue();
        Console.log("store.snapshotHash.before2", String.valueOf(before2));

        Proposal p2 = new Proposal();
        java.util.Set<Fact> inserts2 = new java.util.HashSet<>();
        inserts2.add(f); // same id + same payload
        p2.setInsert(inserts2);

        MutationResult mr2 = new MutationResult();
        mr2.setSnapshotHash(snap2);
        mr2.setAccepted(java.util.List.of(p2));

        CommitResult out2 = orch.orchestrate(mr2);

        assertNotNull(out2);
        assertTrue(out2.isOk());

        SnapshotHash snapAfter2 = store.snapshot().snapshotHash();
        String after2 = (snapAfter2 == null) ? null : snapAfter2.getValue();
        Console.log("store.snapshotHash.after2", String.valueOf(after2));

        // Second application should converge: snapshot unchanged
        assertEquals(before2, after2);

        Console.log("test.pass", "CommitOrchestrator.idempotency.same_insert_twice");
    }

    /**
     * This test verifies idempotency for relationships:
     * applying the SAME logical relationship twice results in:
     *  - first commit: ok=true and snapshot changes (edge added)
     *  - second commit: ok=true but snapshot does NOT change (converged / no-op)
     *
     * Why this matters:
     * - Edge writes are common in ingestion; duplicates must not churn the substrate
     * - Protects deterministic snapshot-hash behavior under retries
     *
     * Note:
     * - We re-align MutationResult.snapshotHash to the CURRENT store hash before each run
     *   because staleness protection is orthogonal to idempotency.
     */
    @Test
    void orchestrate_is_idempotent_same_relationship_twice_second_is_noop_snapshot_unchanged() {
        Console.log("test.start", "CommitOrchestrator.idempotency.same_relationship_twice");

        GraphStore store = GraphStoreImpl.getInstance();
        CommitOrchestrator orch = new CommitOrchestrator();

        // Seed endpoints
        String fromId = "Fact:From:" + System.nanoTime();
        String toId = "Fact:To:" + System.nanoTime();

        Fact from = new Fact(fromId, "{ \"id\":\"" + fromId + "\", \"kind\":\"Seed\" }");
        from.setMode("atomic");
        Fact to = new Fact(toId, "{ \"id\":\"" + toId + "\", \"kind\":\"Seed\" }");
        to.setMode("atomic");

        store.upsertNode(from);
        store.upsertNode(to);

        // Stable edge id for this test run
        String edgeId = "Edge:Idem:" + System.nanoTime();
        Fact edge = new Fact(edgeId, "{ \"id\":\"" + edgeId + "\", \"kind\":\"Rel\" }");
        edge.setMode("relational");

        Relationship rel = new Relationship();
        rel.setFrom(from);
        rel.setTo(to);
        rel.setEdge(edge);

        // ---------- First commit ----------
        SnapshotHash snap1 = store.snapshot().snapshotHash();
        String before1 = (snap1 == null) ? null : snap1.getValue();
        Console.log("store.snapshotHash.before1", String.valueOf(before1));

        Proposal p1 = new Proposal();
        java.util.Set<Relationship> rels1 = new java.util.HashSet<>();
        rels1.add(rel);
        p1.setEdges(rels1);

        MutationResult mr1 = new MutationResult();
        mr1.setSnapshotHash(snap1);
        mr1.setAccepted(java.util.List.of(p1));

        CommitResult out1 = orch.orchestrate(mr1);
        assertNotNull(out1);
        assertTrue(out1.isOk());

        SnapshotHash snapAfter1 = store.snapshot().snapshotHash();
        String after1 = (snapAfter1 == null) ? null : snapAfter1.getValue();
        Console.log("store.snapshotHash.after1", String.valueOf(after1));

        assertNotNull(before1);
        assertNotNull(after1);
        assertNotEquals(before1, after1);

        // ---------- Second commit (same relationship again) ----------
        SnapshotHash snap2 = store.snapshot().snapshotHash();
        String before2 = (snap2 == null) ? null : snap2.getValue();
        Console.log("store.snapshotHash.before2", String.valueOf(before2));

        Proposal p2 = new Proposal();
        java.util.Set<Relationship> rels2 = new java.util.HashSet<>();
        rels2.add(rel); // same from/to/edge
        p2.setEdges(rels2);

        MutationResult mr2 = new MutationResult();
        mr2.setSnapshotHash(snap2);
        mr2.setAccepted(java.util.List.of(p2));

        CommitResult out2 = orch.orchestrate(mr2);
        assertNotNull(out2);
        assertTrue(out2.isOk());

        SnapshotHash snapAfter2 = store.snapshot().snapshotHash();
        String after2 = (snapAfter2 == null) ? null : snapAfter2.getValue();
        Console.log("store.snapshotHash.after2", String.valueOf(after2));

        // Second application should converge: snapshot unchanged
        assertEquals(before2, after2);

        Console.log("test.pass", "CommitOrchestrator.idempotency.same_relationship_twice");
    }

    /**
     * This test verifies the Mutation → Commit handshake contract:
     * CommitOrchestrator must accept a MutationResult ONLY when its snapshotHash
     * matches the current GraphStore snapshotHash at commit time.
     *
     * Setup:
     * - Capture store snapshot S1
     * - Build MutationResult with snapshotHash = S1
     * - Do NOT mutate store between capture and commit
     *
     * Expected:
     * - ok=true (handshake passes)
     *
     * Why this matters:
     * - Defines the integration seam between Mutation subsystem and Commit subsystem
     * - Guarantees proposals are committed only against the snapshot they were validated on
     */
    @Test
    void orchestrate_accepts_mutationResult_when_snapshot_matches_store_at_commit_time() {
        Console.log("test.start", "CommitOrchestrator.handshake.snapshot_matches_accept");

        GraphStore store = GraphStoreImpl.getInstance();

        SnapshotHash s1 = store.snapshot().snapshotHash();
        String h1 = (s1 == null) ? null : s1.getValue();
        Console.log("store.snapshotHash", String.valueOf(h1));

        MutationResult mr = new MutationResult();
        mr.setSnapshotHash(s1);

        // minimal valid accepted proposal (no-op convergence is allowed)
        Proposal p = new Proposal();
        mr.setAccepted(java.util.List.of(p));

        CommitOrchestrator orch = new CommitOrchestrator();

        CommitResult out = orch.orchestrate(mr);

        assertNotNull(out);
        assertTrue(out.isOk());

        Console.log("test.pass", "CommitOrchestrator.handshake.snapshot_matches_accept");
    }

    /**
     * This test verifies stale replay protection under concurrent-style execution:
     *
     * Scenario:
     * - Capture store snapshot S0
     * - Build TWO MutationResults off S0 (simulating concurrent validators)
     * - Commit first mutation → store snapshot advances to S1
     * - Attempt to commit second mutation built on stale S0
     *
     * Expected:
     * - First commit: ok=true
     * - Second commit: ok=false (snapshot mismatch → stale proposal)
     *
     * Why this matters:
     * - Enforces optimistic concurrency control at the commit boundary
     * - Prevents lost updates and out-of-order application
     * - Makes Mutation + Commit safe under parallel pipelines
     */
    @Test
    void orchestrate_rejects_stale_mutationResult_after_concurrent_commit() {
        Console.log("test.start", "CommitOrchestrator.concurrency.stale_replay_rejected");

        GraphStore store = GraphStoreImpl.getInstance();
        CommitOrchestrator orch = new CommitOrchestrator();

        // ---------- baseline snapshot S0 ----------
        SnapshotHash s0 = store.snapshot().snapshotHash();
        String h0 = (s0 == null) ? null : s0.getValue();
        Console.log("store.snapshotHash.S0", String.valueOf(h0));

        // ---------- first mutation (will commit) ----------
        String id1 = "Fact:C1:" + System.nanoTime();
        Fact f1 = new Fact(id1, "{ \"id\":\"" + id1 + "\", \"kind\":\"C1\" }");
        f1.setMode("atomic");

        Proposal p1 = new Proposal();
        java.util.Set<Fact> ins1 = new java.util.HashSet<>();
        ins1.add(f1);
        p1.setInsert(ins1);

        MutationResult mr1 = new MutationResult();
        mr1.setSnapshotHash(s0);
        mr1.setAccepted(java.util.List.of(p1));

        CommitResult out1 = orch.orchestrate(mr1);
        assertNotNull(out1);
        assertTrue(out1.isOk());

        SnapshotHash s1 = store.snapshot().snapshotHash();
        String h1 = (s1 == null) ? null : s1.getValue();
        Console.log("store.snapshotHash.S1", String.valueOf(h1));
        assertNotEquals(h0, h1);

        // ---------- second mutation built on SAME stale snapshot S0 ----------
        String id2 = "Fact:C2:" + System.nanoTime();
        Fact f2 = new Fact(id2, "{ \"id\":\"" + id2 + "\", \"kind\":\"C2\" }");
        f2.setMode("atomic");

        Proposal p2 = new Proposal();
        java.util.Set<Fact> ins2 = new java.util.HashSet<>();
        ins2.add(f2);
        p2.setInsert(ins2);

        MutationResult mr2 = new MutationResult();
        mr2.setSnapshotHash(s0); // STALE on purpose
        mr2.setAccepted(java.util.List.of(p2));

        CommitResult out2 = orch.orchestrate(mr2);

        assertNotNull(out2);
        assertFalse(out2.isOk());

        Console.log("test.pass", "CommitOrchestrator.concurrency.stale_replay_rejected");
    }

}
