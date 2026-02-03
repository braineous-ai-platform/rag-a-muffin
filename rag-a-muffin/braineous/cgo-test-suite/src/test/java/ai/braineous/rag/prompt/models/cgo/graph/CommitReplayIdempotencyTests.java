package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import ai.braineous.rag.prompt.models.cgo.graph.commit.CommitOrchestrator;
import ai.braineous.rag.prompt.models.cgo.graph.commit.CommitResult;
import ai.braineous.rag.prompt.models.cgo.graph.mutation.MutationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommitReplayIdempotencyTests {

    private MongoClient mongoClient;
    private static final String MONGO_URI = "mongodb://localhost:27017";
    private GraphStoreMongo store;

    @BeforeEach
    public void setup(){
        GraphBuilder.getInstance().clear();

        mongoClient = com.mongodb.client.MongoClients.create(MONGO_URI);

        store = new GraphStoreMongo(mongoClient);

        mongoClient.getDatabase(GraphStoreMongo.DEFAULT_DB_NAME)
                .getCollection(GraphStoreMongo.DEFAULT_NODE_COLLECTION_NAME)
                .deleteMany(new Document());

        mongoClient.getDatabase(GraphStoreMongo.DEFAULT_DB_NAME)
                .getCollection(GraphStoreMongo.DEFAULT_EDGE_COLLECTION_NAME)
                .deleteMany(new Document());

    }

    @Test
    void commit_replay_same_mutationResult_is_rejected_and_state_unchanged() {
        Console.log("test.start", "commit.replay.same_mr.rejected_no_drift");

        GraphBuilder gb = GraphBuilder.getInstance();
        gb.clear();

        // minimal graph
        Fact aus = new Fact("Airport:AUS", "{ \"id\":\"Airport:AUS\" }"); aus.setMode("atomic");
        Fact dfw = new Fact("Airport:DFW", "{ \"id\":\"Airport:DFW\" }"); dfw.setMode("atomic");
        gb.addNode(aus); gb.addNode(dfw);

        Fact edge = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\" }");
        edge.setMode("relational");

        // Build ONE proposal and run it through the real pipeline via bind()
        // bind() internally builds proposals -> mutation -> commit, but we need the MutationResult.
        // So we recreate a minimal MutationResult manually for commit replay.

        GraphStore store = GraphStoreImpl.getInstance();
        String beforeHash = store.snapshot().snapshotHash().getValue();

        // Proposal: add the edge via relationship mutate (store.mutate)
        Relationship rel = new Relationship();
        rel.setFrom(aus);
        rel.setTo(dfw);
        rel.setEdge(edge);

        Proposal p = new Proposal();
        p.setEdges(new java.util.HashSet<>(java.util.Set.of(rel)));

        MutationResult mr = new MutationResult();
        mr.setSnapshotHash(store.snapshot().snapshotHash());
        mr.setAccepted(java.util.List.of(p));
        //mr.setOk(true);

        CommitOrchestrator co = CommitOrchestrator.getInstance();

        // First commit should succeed
        CommitResult c1 = co.orchestrate(mr);
        assertNotNull(c1);
        assertTrue(c1.isOk());

        String after1 = store.snapshot().snapshotHash().getValue();
        assertNotEquals(beforeHash, after1);

        // Replay same mr should fail due to snapshot mismatch (stale)
        CommitResult c2 = co.orchestrate(mr);
        assertNotNull(c2);
        assertFalse(c2.isOk(), "replay must be rejected as stale");

        String after2 = store.snapshot().snapshotHash().getValue();
        assertEquals(after1, after2, "rejected replay must not drift state");
    }

    @Test
    void commit_replay_with_refreshed_snapshotHash_of_noop_is_ok_and_no_drift() {
        Console.log("test.start", "commit.replay.refreshed_hash.noop.ok_no_drift");

        GraphBuilder gb = GraphBuilder.getInstance();
        gb.clear();

        GraphStore store = GraphStoreImpl.getInstance();
        CommitOrchestrator co = CommitOrchestrator.getInstance();

        // No-op proposal: delete a missing node (allowed idempotent)
        Fact ghost = new Fact("Ghost:DOES_NOT_EXIST", "{ \"id\":\"Ghost:DOES_NOT_EXIST\" }");
        ghost.setMode("atomic");

        Proposal p = new Proposal();
        p.setDelete(new java.util.HashSet<>(java.util.Set.of(ghost)));

        // Commit 1
        MutationResult mr1 = new MutationResult();
        mr1.setSnapshotHash(store.snapshot().snapshotHash());
        mr1.setAccepted(java.util.List.of(p));
        //mr1.setOk(true);

        String before = store.snapshot().snapshotHash().getValue();

        CommitResult c1 = co.orchestrate(mr1);
        assertNotNull(c1);
        assertTrue(c1.isOk());

        String after1 = store.snapshot().snapshotHash().getValue();
        assertEquals(before, after1, "noop commit must not change hash");

        // "Replay" as a new event: same proposal but refreshed snapshotHash
        MutationResult mr2 = new MutationResult();
        mr2.setSnapshotHash(store.snapshot().snapshotHash());
        mr2.setAccepted(java.util.List.of(p));
        //mr2.setOk(true);

        CommitResult c2 = co.orchestrate(mr2);
        assertNotNull(c2);
        assertTrue(c2.isOk());

        String after2 = store.snapshot().snapshotHash().getValue();
        assertEquals(after1, after2, "noop replay must not drift state");
    }

}
