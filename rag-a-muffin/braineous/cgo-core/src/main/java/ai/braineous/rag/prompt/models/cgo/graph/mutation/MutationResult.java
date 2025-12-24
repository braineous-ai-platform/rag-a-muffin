package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;
import ai.braineous.rag.prompt.models.cgo.graph.SnapshotHash;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MutationResult {
    // field init helper (thread-safe)
    private static final java.util.concurrent.atomic.AtomicLong EVENT_SEQ =
            new java.util.concurrent.atomic.AtomicLong(0L);
    private String id;

    private String mutationEventId;

    private List<Proposal> accepted = new ArrayList<>();

    private List<Proposal> rejected = new ArrayList<>();

    private MutationResultListener resultListener;

    private SnapshotHash snapshotHash;

    public MutationResult() {
        long seq = MutationResult.nextSeq();
        this.id = MutationResult.nextEventId(seq);
    }

    public String getId() {
        return id;
    }

    public List<Proposal> getAccepted() {
        return accepted;
    }

    public void setAccepted(List<Proposal> accepted) {
        this.accepted = accepted;
    }

    public List<Proposal> getRejected() {
        return rejected;
    }

    public void setRejected(List<Proposal> rejected) {
        this.rejected = rejected;
    }

    public String getMutationEventId() {
        return mutationEventId;
    }

    public void setMutationEventId(String mutationEventId) {
        this.mutationEventId = mutationEventId;
    }

    public MutationResultListener getResultListener() {
        return resultListener;
    }

    public void setResultListener(MutationResultListener resultListener) {
        this.resultListener = resultListener;
    }

    public SnapshotHash getSnapshotHash() {
        return snapshotHash;
    }

    public void setSnapshotHash(SnapshotHash snapshotHash) {
        this.snapshotHash = snapshotHash;
    }

    @Override
    public String toString() {
        return "MutationResult{" +
                "id='" + id + '\'' +
                ", mutationEventId='" + mutationEventId + '\'' +
                ", accepted=" + accepted +
                ", rejected=" + rejected +
                ", snapshotHash=" + snapshotHash +
                '}';
    }

    public JsonObject toJson() {
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();

        json.addProperty("id", this.id);
        json.addProperty("snapshotHash", this.snapshotHash.getValue());
        json.addProperty("mutationEventId", this.mutationEventId);

        com.google.gson.JsonArray accepted = new com.google.gson.JsonArray();
        for (Proposal p : this.accepted) {
            accepted.add(p.getId()); // or p.toJson() later if you want depth
        }
        json.add("accepted", accepted);

        com.google.gson.JsonArray rejected = new com.google.gson.JsonArray();
        for (Proposal p : this.rejected) {
            rejected.add(p.getId()); // or p.toJson() later if you want depth
        }
        json.add("rejected", rejected);

        return json;
    }

    // call inside constructor
    private static long nextSeq() {
        return EVENT_SEQ.incrementAndGet();
    }

    // call inside constructor
    private static String nextEventId(long seq) {
        // readable + sortable-ish (by seq)
        return "MR-" + seq;
    }
}
