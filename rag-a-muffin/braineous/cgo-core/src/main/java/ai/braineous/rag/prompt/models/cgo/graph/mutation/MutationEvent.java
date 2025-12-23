package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;
import ai.braineous.rag.prompt.models.cgo.graph.SnapshotHash;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

class MutationEvent {
    // field init helper (thread-safe)
    private static final java.util.concurrent.atomic.AtomicLong EVENT_SEQ =
            new java.util.concurrent.atomic.AtomicLong(0L);

    private final String id;

    private Set<Proposal> proposals = new HashSet<>();

    private SnapshotHash snapshotHash;

    private final long createdAt; //epoch

    private MutationResultListener resultListener;

    public MutationEvent() {
        long seq = MutationEvent.nextSeq();
        this.id = MutationEvent.nextEventId(seq);
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public Set<Proposal> getProposals() {
        return proposals;
    }

    public void setProposals(Set<Proposal> proposals) {
        this.proposals = proposals;
    }

    public SnapshotHash getSnapshotHash() {
        return snapshotHash;
    }

    public void setSnapshotHash(SnapshotHash snapshotHash) {
        this.snapshotHash = snapshotHash;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public MutationResultListener getResultListener() {
        return resultListener;
    }

    public void setResultListener(MutationResultListener resultListener) {
        this.resultListener = resultListener;
    }

    @Override
    public String toString() {
        return "MutationEvent{" +
                "id='" + id + '\'' +
                ", proposals=" + proposals +
                ", snapshotHash=" + snapshotHash +
                ", createdAt=" + createdAt +
                '}';
    }

    public JsonObject toJson() {
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();

        json.addProperty("id", this.id);
        json.addProperty("snapshotHash", this.snapshotHash.getValue());
        json.addProperty("createdAt", this.createdAt);

        com.google.gson.JsonArray props = new com.google.gson.JsonArray();
        for (Proposal p : this.proposals) {
            props.add(p.getId()); // or p.toJson() later if you want depth
        }
        json.add("proposals", props);

        return json;
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
